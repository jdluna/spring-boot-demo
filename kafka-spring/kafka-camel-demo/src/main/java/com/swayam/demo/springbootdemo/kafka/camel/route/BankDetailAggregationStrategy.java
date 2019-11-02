package com.swayam.demo.springbootdemo.kafka.camel.route;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import com.swayam.demo.springbootdemo.kafkadto.JobCount;

public class BankDetailAggregationStrategy implements AggregationStrategy, Predicate {

    private final NamedParameterJdbcOperations jdbcTemplate;

    public BankDetailAggregationStrategy(NamedParameterJdbcOperations jdbcTemplate) {
	this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
	if (oldExchange == null) {
	    // insert only the first message in the group
	    persistMessageOffsetDetails(newExchange);
	    return newExchange;
	}

	JobCount partialResults = oldExchange.getIn().getBody(JobCount.class);
	JobCount newMessage = newExchange.getIn().getBody(JobCount.class);

	// any header that is used by the predicate should be set in the message
	// returned from this method
	Boolean shouldComplete = newExchange.getIn()
		.getHeader(RouteConstants.COMPLETE_JOB_AGGREGATION_COMMAND, Boolean.class);
	if (shouldComplete != null) {
	    oldExchange.getIn().setHeader(RouteConstants.COMPLETE_JOB_AGGREGATION_COMMAND,
		    shouldComplete);
	}

	oldExchange.getIn().setBody(doAggregation(newMessage, partialResults), JobCount.class);
	return oldExchange;
    }

    private JobCount doAggregation(JobCount newMessage, JobCount partialResults) {
	JobCount aggregate = new JobCount();

	aggregate.setAdminCount(newMessage.getAdminCount() + partialResults.getAdminCount());
	aggregate.setBlueCollarCount(
		newMessage.getBlueCollarCount() + partialResults.getBlueCollarCount());
	aggregate.setEntrepreneurCount(
		newMessage.getEntrepreneurCount() + partialResults.getEntrepreneurCount());
	aggregate.setHouseMaidCount(
		newMessage.getHouseMaidCount() + partialResults.getHouseMaidCount());
	aggregate.setManagementCount(
		newMessage.getManagementCount() + partialResults.getManagementCount());
	aggregate.setRetiredCount(newMessage.getRetiredCount() + partialResults.getRetiredCount());
	aggregate.setSelfEmployedCount(
		newMessage.getSelfEmployedCount() + partialResults.getSelfEmployedCount());
	aggregate.setServicesCount(
		newMessage.getServicesCount() + partialResults.getServicesCount());
	aggregate.setStudentCount(newMessage.getStudentCount() + partialResults.getStudentCount());
	aggregate.setTechnicianCount(
		newMessage.getTechnicianCount() + partialResults.getTechnicianCount());
	aggregate.setUnemployedCount(
		newMessage.getUnemployedCount() + partialResults.getUnemployedCount());
	aggregate.setUnknownCount(newMessage.getUnknownCount() + partialResults.getUnknownCount());

	return aggregate;
    }

    private void persistMessageOffsetDetails(Exchange message) {
	String sql =
		"INSERT INTO message_outbox (correlation_id, processor_id, topic_name, partition_id, offset) VALUES (:correlationId, :processorId, :topicName, :partitionId, :offset)";
	Map<String, Object> params = new HashMap<>();
	params.put("correlationId", message.getIn().getHeader(KafkaConstants.KEY, String.class));
	String topicName = message.getIn().getHeader(KafkaConstants.TOPIC, String.class);
	// TODO: i dont want to insert records which are being replayed
	// FIXME: come-up with a better solution
	if (topicName == null) {
	    return;
	}
	params.put("topicName", topicName);
	params.put("partitionId",
		message.getIn().getHeader(KafkaConstants.PARTITION, Integer.class));
	params.put("offset", message.getIn().getHeader(KafkaConstants.OFFSET, Long.class));
	params.put("processorId", System.getProperty("processorId"));

	jdbcTemplate.update(sql, params);

    }

    @Override
    public boolean matches(Exchange oldExchange) {
	Boolean shouldComplete = oldExchange.getIn()
		.getHeader(RouteConstants.COMPLETE_JOB_AGGREGATION_COMMAND, Boolean.class);
	return shouldComplete == null ? false : shouldComplete;
    }

}