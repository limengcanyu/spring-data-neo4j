package org.springframework.data.neo4j.integration.properties;

import java.beans.IntrospectionException;
import java.util.Objects;

import org.aopalliance.intercept.MethodInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.neo4j.core.mapping.IdMixin;
import org.springframework.data.neo4j.core.mapping.IdMixinAdvice;
import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext;
import org.springframework.data.neo4j.core.mapping.Neo4jPersistentEntity;
import org.springframework.data.neo4j.core.mapping.Neo4jPersistentProperty;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;

public class ProxyExperiments {

	@Test
	public void proxy_of_concrete_class_is_created() {
		ProxyFactory pf = new ProxyFactory();
		pf.setTargetClass(ConcreteClass.class);
		pf.addAdvice((MethodInterceptor) invocation -> "concreteWork".equals(invocation.getMethod().getName())
				? "magic"
				: invocation.proceed());

		Object result = pf.getProxy();

		System.out.println(result);
	}

	@Test
	void f() throws IntrospectionException {

		Neo4jMappingContext ctx = new Neo4jMappingContext();
		Neo4jPersistentEntity<?> e = ctx.getPersistentEntity(NeedsCGLIBProxy.class);
		/*
		KotlinClub c = new KotlinClub(1L, "asd");
		ProxyFactory pfx = new ProxyFactory(c);
		pfx.setProxyTargetClass(true);
		pfx.getProxy();*/
		System.out.println(e.getIdDescription());

		//System.out.println(e.getPropertyAccessor("").set);
		NeedsCGLIBProxy withPropSupport = new NeedsCGLIBProxy();
	//	withPropSupport.setS("asd");

		ProxyFactory pf = new ProxyFactory(withPropSupport);
		pf.addInterface(IdMixin.class);
		pf.addAdvice(new IdMixinAdvice());
		pf.setProxyTargetClass(true);
		pf.setOptimize(true);

		NeedsCGLIBProxy f = (NeedsCGLIBProxy) pf.getProxy();

		//Neo4jPersistentProperty npp = new DefaultNeo4jPersistentProperty(p, e, null, SimpleTypeHolder.DEFAULT);

		PersistentPropertyAccessor x = e.getPropertyAccessor(f);
		System.out.println(x.getProperty(e.getIdProperty()));
		x.setProperty(e.getIdProperty(), 4711L);
		System.out.println(x.getProperty(e.getIdProperty()));
		Neo4jPersistentProperty sp = e.getPersistentProperty("s");
		System.out.println("setting property");
		f.setS("x");
		x.setProperty(sp, "hallo");
		f = (NeedsCGLIBProxy) x.getBean();
		System.out.println("getting via accessor ");
		System.out.println(x.getProperty(sp));
		System.out.println("getting via rthing");
		System.out.println(f.getS());
	}



private interface Interface {
	String doWork(int param);
}

private interface OtherInterface {
	int process(int input);
}

static class ConcreteClass {
	public String concreteWork(int param) {
		return "asd";
	}
}

final class NonProxiable {
	private String s;

	public String getS() {
		return s;
	}

	public void setS(String s) {
		this.s = s;
	}
}

@RelationshipProperties
private static class NeedsCGLIBProxy {

	private String s;

	public String getS() {
		return s;
	}

	public void setS(String s) {
		System.out.println("Noch mal, willst du mich verarschen? + s"+ s);
		this.s = s;
	}

	@Override public boolean equals(Object o) {
		System.out.println("me");
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		NeedsCGLIBProxy that = (NeedsCGLIBProxy) o;
		return Objects.equals(s, that.s);
	}

	@Override public int hashCode() {
		return Objects.hash(s);
	}
}

interface MyAwesomeProps {

}

class IAmNotSureYet implements MyAwesomeProps {

}
}
