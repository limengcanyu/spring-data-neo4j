package org.springframework.data.neo4j.core.mapping;

import java.util.function.Function;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.PersistentPropertyAccessor;

class IdHolderAwarePersistentPropertyAccessor<B> implements PersistentPropertyAccessor<B> {

	private final Function<B, PersistentPropertyAccessor<B>> propertAccessorSupplier;
	private B currentBean;

	PersistentPropertyAccessor<B> delegate;

	public IdHolderAwarePersistentPropertyAccessor(
			Function<B, PersistentPropertyAccessor<B>> propertAccessorSupplier, B currentBean) {
		this.propertAccessorSupplier = propertAccessorSupplier;

		if (currentBean instanceof IdMixin) {
			currentBean = currentBean;
		} else {
			ProxyFactory pf = new ProxyFactory(currentBean);
			pf.setProxyTargetClass(true);
			pf.addInterface(IdMixin.class);
			pf.addAdvice(new IdMixinAdvice());

			currentBean = (B) pf.getProxy();
		}
		delegate = propertAccessorSupplier.apply(currentBean);
	}

	@Override
	public void setProperty(PersistentProperty<?> property, Object value) {

		if ("id".equals(property.getName())) {
			((IdMixin) delegate.getBean()).setId((Long) value);
		} else {
			delegate.setProperty(property, value);
		}
	}

	@Override
	public Object getProperty(PersistentProperty<?> property) {
		if ("id".equals(property.getName())) {
			return ((IdMixin) delegate.getBean()).getId();
		}
		return delegate.getProperty(property);
	}

	@Override
	public B getBean() {
		return delegate.getBean();
	}
}
