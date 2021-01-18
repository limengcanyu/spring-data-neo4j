package org.springframework.data.neo4j.integration.movies;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Version;
import org.springframework.data.neo4j.config.AbstractNeo4jConfig;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;
import org.springframework.data.neo4j.test.Neo4jExtension;
import org.springframework.data.neo4j.test.Neo4jIntegrationTest;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Neo4jIntegrationTest
public class PropertyIT {

	protected static Neo4jExtension.Neo4jConnectionSupport neo4jConnectionSupport;

	@BeforeAll
	static void setupData(@Autowired Driver driver) {

		try (Session session = driver.session()) {
			session.run("MATCH (n) DETACH DELETE n").consume();
		}
	}

	@Test // GH-2118
	void assignedIdNoVersionShouldNotOverwriteUnknownProperties(@Autowired Driver driver, @Autowired Neo4jTemplate template) {

		try (Session session = driver.session()) {
			session.run("CREATE (m:SimplePropertyContainer {id: 'id1', knownProperty: 'A', unknownProperty: 'Mr. X'}) RETURN id(m)").consume();
		}

		updateKnownAndAssertUnknownProperty(driver, template, SimplePropertyContainer.class, "id1");
	}

	@Test // GH-2118
	void assignedIdWithVersionShouldNotOverwriteUnknownProperties(@Autowired Driver driver, @Autowired Neo4jTemplate template) {

		try (Session session = driver.session()) {
			session.run("CREATE (m:SimplePropertyContainerWithVersion {id: 'id1', version: 1, knownProperty: 'A', unknownProperty: 'Mr. X'}) RETURN id(m)").consume();
		}

		updateKnownAndAssertUnknownProperty(driver, template, SimplePropertyContainerWithVersion.class, "id1");
	}

	@Test // GH-2118
	void generatedIdNoVersionShouldNotOverwriteUnknownProperties(@Autowired Driver driver, @Autowired Neo4jTemplate template) {

		Long id;
		try (Session session = driver.session()) {
			id = session.run("CREATE (m:SimpleGeneratedIDPropertyContainer {knownProperty: 'A', unknownProperty: 'Mr. X'}) RETURN id(m)").single().get(0).asLong();
		}

		updateKnownAndAssertUnknownProperty(driver, template, SimpleGeneratedIDPropertyContainer.class, id);
	}

	@Test // GH-2118
	void generatedIdWithVersionShouldNotOverwriteUnknownProperties(@Autowired Driver driver, @Autowired Neo4jTemplate template) {

		Long id;
		try (Session session = driver.session()) {
			id = session.run("CREATE (m:SimpleGeneratedIDPropertyContainerWithVersion {version: 1, knownProperty: 'A', unknownProperty: 'Mr. X'}) RETURN id(m)").single().get(0).asLong();
		}

		updateKnownAndAssertUnknownProperty(driver, template, SimpleGeneratedIDPropertyContainerWithVersion.class, id);
	}

	private static void updateKnownAndAssertUnknownProperty(Driver driver, Neo4jTemplate template, Class<? extends BaseClass> type, Object id) {

		Optional<? extends BaseClass> optionalContainer = template.findById(id, type);
		assertThat(optionalContainer).isPresent();
		optionalContainer.ifPresent(m -> {
			m.setKnownProperty("A2");
			template.save(m);
		});

		try (Session session = driver.session()) {
			long cnt = session
					.run("MATCH (m:" + type.getSimpleName() + ") WHERE " + (id instanceof Long ? "id(m) " : "m.id")
						 + " = $id AND m.knownProperty = 'A2' AND m.unknownProperty = 'Mr. X' RETURN count(m)",
							Collections.singletonMap("id", id)).single().get(0).asLong();
			assertThat(cnt).isEqualTo(1L);
		}
	}

	@Test
	void multipleAssignedIdNoVersionShouldNotOverwriteUnknownProperties(@Autowired Driver driver, @Autowired Neo4jTemplate template) {

		try (Session session = driver.session()) {
			session.run("CREATE (m:SimplePropertyContainer {id: 'a', knownProperty: 'A', unknownProperty: 'Fix'})  RETURN id(m)").consume();
			session.run("CREATE (m:SimplePropertyContainer {id: 'b', knownProperty: 'B', unknownProperty: 'Foxy'}) RETURN id(m)").consume();
		}

		SimplePropertyContainer optionalContainerA = template.findById("a", SimplePropertyContainer.class).get();
		SimplePropertyContainer optionalContainerB = template.findById("b", SimplePropertyContainer.class).get();
		optionalContainerA.setKnownProperty("A2");
		optionalContainerB.setKnownProperty("B2");

		template.saveAll(Arrays.asList(optionalContainerA, optionalContainerB));

		try (Session session = driver.session()) {
			long cnt = session.run("MATCH (m:SimplePropertyContainer) WHERE m.id in $ids AND m.unknownProperty IS NOT NULL RETURN count(m)",
					Collections.singletonMap("ids", Arrays.asList("a", "b"))).single().get(0).asLong();
			assertThat(cnt).isEqualTo(2L);
		}
	}

	@Test
	void relationshipPropertiesMustNotBeOverwritten(@Autowired Driver driver, @Autowired Neo4jTemplate template) {

		Long id;
		try (Session session = driver.session()) {
			id = session.run("CREATE (a:IrrelevantSourceContainer) - [:RELATIONSHIP_PROPERTY_CONTAINER {knownProperty: 'A', unknownProperty: 'Mr. X'}] -> (:IrrelevantTargetContainer) RETURN id(a)").single().get(0).asLong();
		}

		Optional<IrrelevantSourceContainer> optionalContainer = template.findById(id, IrrelevantSourceContainer.class);
		assertThat(optionalContainer).hasValueSatisfying(c -> {
			assertThat(c.getRelationshipPropertyContainer()).isNotNull();
			assertThat(c.getRelationshipPropertyContainer().getId()).isNotNull();
		});

		optionalContainer.ifPresent(m -> {
			m.getRelationshipPropertyContainer().setKnownProperty("A2");;
			template.save(m);
		});

		try (Session session = driver.session()) {
			long cnt = session.run("MATCH (m) - [r:RELATIONSHIP_PROPERTY_CONTAINER] -> (:IrrelevantTargetContainer) WHERE id(m) = $id AND r.knownProperty = 'A2' AND r.unknownProperty = 'Mr. X' RETURN count(m)",
					Collections.singletonMap("id", id)).single().get(0).asLong();
			assertThat(cnt).isEqualTo(1L);
		}
	}

	@Test
	void relationshipIdsShouldBeFilled(@Autowired Driver driver, @Autowired Neo4jTemplate template) {

		RelationshipPropertyContainer rel = new RelationshipPropertyContainer();
		rel.setKnownProperty("A");
		rel.setIrrelevantTargetContainer(new IrrelevantTargetContainer());
		IrrelevantSourceContainer s = template.save(new IrrelevantSourceContainer(rel));

		assertThat(s.getRelationshipPropertyContainer().getId()).isNotNull();
		try (Session session = driver.session()) {
			long cnt = session.run("MATCH (m) - [r:RELATIONSHIP_PROPERTY_CONTAINER] -> (:IrrelevantTargetContainer) WHERE id(m) = $id AND r.knownProperty = 'A' RETURN count(m)",
					Collections.singletonMap("id", s.getId())).single().get(0).asLong();
			assertThat(cnt).isEqualTo(1L);
		}
	}

	@Getter @Setter
	static abstract class BaseClass {

		private String knownProperty;
	}

	@Node
	@Getter @Setter
	static class SimplePropertyContainer extends BaseClass {

		@Id
		private String id;
	}

	@Node
	@Getter @Setter
	static class SimplePropertyContainerWithVersion extends SimplePropertyContainer {

		@Version
		private Long version;
	}

	@Node
	@Getter @Setter
	static class SimpleGeneratedIDPropertyContainer extends BaseClass {

		@Id @GeneratedValue
		private Long id;
	}

	@Node
	@Getter @Setter
	static class SimpleGeneratedIDPropertyContainerWithVersion extends SimpleGeneratedIDPropertyContainer {

		@Version
		private Long version;
	}

	@Node
	@Getter @Setter
	static class IrrelevantSourceContainer {
		@Id @GeneratedValue
		private Long id;

		@Relationship(type = "RELATIONSHIP_PROPERTY_CONTAINER")
		RelationshipPropertyContainer relationshipPropertyContainer;

		IrrelevantSourceContainer(
				RelationshipPropertyContainer relationshipPropertyContainer) {
			this.relationshipPropertyContainer = relationshipPropertyContainer;
		}
	}

	@RelationshipProperties
	@Getter @Setter
	static class RelationshipPropertyContainer extends BaseClass {

		@Id @GeneratedValue
		private Long id;

		@TargetNode
		private IrrelevantTargetContainer irrelevantTargetContainer;
	}

	@Node
	static class IrrelevantTargetContainer {
		@Id @GeneratedValue
		private Long id;
	}

	@Configuration
	@EnableTransactionManagement
	static class Config extends AbstractNeo4jConfig {

		@Bean
		public Driver driver() {
			return neo4jConnectionSupport.getDriver();
		}
	}
}
