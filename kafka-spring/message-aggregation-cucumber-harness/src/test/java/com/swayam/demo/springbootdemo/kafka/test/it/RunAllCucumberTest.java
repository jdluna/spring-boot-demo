package com.swayam.demo.springbootdemo.kafka.test.it;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(features = { "src/test/resources/features" }, plugin = { "pretty", "html:target/cucumber-html-report", "junit:target/cucumber-junit-report/allcukes.xml" },
	glue = { "com.swayam.demo.springbootdemo.kafka.test.glue" })
public class RunAllCucumberTest {

}