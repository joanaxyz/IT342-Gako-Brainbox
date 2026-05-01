package edu.cit.gako.brainbox;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;

@SpringBootApplication
public class BrainboxApiApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
    	dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
    	SpringApplication.run(BrainboxApiApplication.class, args);
	}
}
