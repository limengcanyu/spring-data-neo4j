package org.springframework.data.neo4j.core.mapping;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public final class IdHolderImpl implements MethodInterceptor {

	private Long id;

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		switch (invocation.getMethod().getName()) {
			case "setId":
				this.id = (Long) invocation.getArguments()[0];
				return null;
			case "getId":
				return this.id;
			default: {
				invocation.
				System.out.println("peng " + invocation.getMethod().getName());
				return invocation.proceed();
			}
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
