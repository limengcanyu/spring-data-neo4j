package org.springframework.data.neo4j.integration.movies;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.Getter;
import lombok.Setter;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
import org.springframework.data.neo4j.config.AbstractReactiveNeo4jConfig;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.data.neo4j.core.ReactiveNeo4jTemplate;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.repository.config.EnableReactiveNeo4jRepositories;
import org.springframework.data.neo4j.test.Neo4jExtension;
import org.springframework.data.neo4j.test.Neo4jIntegrationTest;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Neo4jIntegrationTest
public class ReactivePropertyIT {

	protected static Neo4jExtension.Neo4jConnectionSupport neo4jConnectionSupport;

	@BeforeAll
	static void setupData(@Autowired Driver driver) {

		try (Session session = driver.session()) {
			session.run("MATCH (n) DETACH DELETE n").consume();
		}
	}

	@Test
	void relationshipIdsShouldBeFilled(@Autowired Driver driver, @Autowired ReactiveNeo4jTemplate template) {

		PropertyIT.RelationshipPropertyContainer rel = new PropertyIT.RelationshipPropertyContainer();
		rel.setKnownProperty("A");
		rel.setIrrelevantTargetContainer(new PropertyIT.IrrelevantTargetContainer());

		List<PropertyIT.IrrelevantSourceContainer> recorded = new ArrayList<>();
		template.save(new PropertyIT.IrrelevantSourceContainer(rel))
				.as(StepVerifier::create)
				.recordWith(() -> recorded)
				.expectNextMatches(i -> i.getRelationshipPropertyContainer().getId() != null)
				.verifyComplete();

		try (Session session = driver.session()) {
			long cnt = session.run("MATCH (m) - [r:RELATIONSHIP_PROPERTY_CONTAINER] -> (:IrrelevantTargetContainer) WHERE id(m) = $id AND r.knownProperty = 'A' RETURN count(m)",
					Collections.singletonMap("id", recorded.get(0).getId())).single().get(0).asLong();
			assertThat(cnt).isEqualTo(1L);
		}
	}

	@Configuration
	@EnableTransactionManagement
	static class Config extends AbstractReactiveNeo4jConfig {

		@Bean
		public Driver driver() {
			return neo4jConnectionSupport.getDriver();
		}
	}
}
