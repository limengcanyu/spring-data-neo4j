package org.springframework.data.neo4j.core.mapping;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.function.Function;

import org.neo4j.driver.Value;
import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.data.util.TypeInformation;

final class IdHolderIdProperty implements Neo4jPersistentProperty {

	private static final Property p;

	static {
		try {
			p = Property.of(
					ClassTypeInformation.from(IdMixin.class),
					new PropertyDescriptor("id", IdMixin.class, "getId","setId"));
		} catch (IntrospectionException e) {
			throw new RuntimeException(e);
		}
	}

	private final Neo4jPersistentEntity<?> owner;

	IdHolderIdProperty(Neo4jPersistentEntity<?> owner) {
		this.owner = owner;
	}

	@Override
	public PersistentEntity<?, Neo4jPersistentProperty> getOwner() {
		return owner;
	}

	@Override public String getName() {
		return "id";
	}

	@Override public Class<?> getType() {
		return Long.class;
	}

	@Override public TypeInformation<?> getTypeInformation() {
		return ClassTypeInformation.from(Long.class);
	}

	@Override public Iterable<? extends TypeInformation<?>> getPersistentEntityTypes() {
		return Collections.emptySet();
	}

	@Override
	public Method getGetter() {
		return p.getGetter().get();
	}

	@Override
	public Method getSetter() {
		return p.getSetter().get();
	}

	@Override public Method getWither() {
		return null;
	}

	@Override public Field getField() {
		return null;
	}

	@Override public String getSpelExpression() {
		return null;
	}

	@Override public Association<Neo4jPersistentProperty> getAssociation() {
		return null;
	}

	@Override public boolean isEntity() {
		return false;
	}

	@Override public String getFieldName() {
		return null;
	}

	@Override public String getPropertyName() {
		return null;
	}

	@Override public boolean isIdProperty() {
		return true;
	}

	@Override public boolean isInternalIdProperty() {
		return true;
	}

	@Override public boolean isVersionProperty() {
		return false;
	}

	@Override public boolean isCollectionLike() {
		return false;
	}

	@Override public boolean isMap() {
		return false;
	}

	@Override public boolean isArray() {
		return false;
	}

	@Override public boolean isTransient() {
		return false;
	}

	@Override public boolean isWritable() {
		return true;
	}

	@Override public boolean isImmutable() {
		return false;
	}

	@Override public boolean isAssociation() {
		return false;
	}

	@Override public Class<?> getComponentType() {
		return null;
	}

	@Override public Class<?> getRawType() {
		return Long.class;
	}

	@Override public Class<?> getMapValueType() {
		return null;
	}

	@Override public Class<?> getActualType() {
		return null;
	}

	@Override public boolean isRelationship() {
		return false;
	}

	@Override public boolean isComposite() {
		return false;
	}

	@Override public <A extends Annotation> A findAnnotation(Class<A> annotationType) {
		return null;
	}

	@Override public <A extends Annotation> A findPropertyOrOwnerAnnotation(Class<A> annotationType) {
		return null;
	}

	@Override public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
		return false;
	}

	@Override public boolean usePropertyAccess() {
		return true;
	}

	@Override public Class<?> getAssociationTargetType() {
		return null;
	}

	@Override public Function<Object, Value> getOptionalWritingConverter() {
		return null;
	}

	@Override public Function<Value, Object> getOptionalReadingConverter() {
		return null;
	}

	@Override public boolean isEntityWithRelationshipProperties() {
		return false;
	}
}
