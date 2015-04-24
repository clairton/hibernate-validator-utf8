package br.eti.clairton.validation;

import static javax.validation.Validation.buildDefaultValidatorFactory;
import static org.junit.Assert.assertEquals;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;

import org.junit.Before;
import org.junit.Test;

public class UTF8ResourceBundleMessageInterpolatorTest {
	private Validator validator;
	
	@Before
	public void init(){
		validator = buildDefaultValidatorFactory().getValidator();
	}
	
	@Test
	public void test() {
		final Set<ConstraintViolation<Teste>> m = validator.validate(new Teste());
		final ConstraintViolation<Teste> violation = m.iterator().next();
		assertEquals("Acentuação",violation.getMessage());
	}

	
	private static class Teste{
		@NotNull(message="{teste}")
		private String valor;
	}
}
