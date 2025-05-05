package com.uor.eng.config;

import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration.AccessLevel;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for WebConfig
 */
public class WebConfigTest {

  /**
   * Tests that the ModelMapper bean is created properly
   */
  @Test
  public void testModelMapperBeanCreation() {
    // Create an instance of WebConfig
    WebConfig webConfig = new WebConfig();

    // Call the bean method directly
    ModelMapper modelMapper = webConfig.modelMapper();

    // Assert that the returned object is not null and is a ModelMapper instance
    assertNotNull(modelMapper, "ModelMapper should not be null");
    assertTrue(modelMapper instanceof ModelMapper, "Bean should be an instance of ModelMapper");
  }

  /**
   * Tests that the ModelMapper bean is properly registered in the Spring context
   */
  @Test
  public void testModelMapperBeanRegistration() {
    // Create a Spring application context with our configuration
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    context.register(WebConfig.class);
    context.refresh();

    try {
      // Get the ModelMapper bean from the context
      ModelMapper modelMapper = context.getBean(ModelMapper.class);

      // Assert that the bean is found and is a ModelMapper instance
      assertNotNull(modelMapper, "ModelMapper bean should be registered in the context");
    } finally {
      // Always close the context to release resources
      context.close();
    }
  }

  /**
   * Tests that only one ModelMapper bean is created (singleton scope)
   */
  @Test
  public void testModelMapperSingletonScope() {
    // Create a Spring application context with our configuration
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    context.register(WebConfig.class);
    context.refresh();

    try {
      // Get the ModelMapper bean twice from the context
      ModelMapper firstInstance = context.getBean(ModelMapper.class);
      ModelMapper secondInstance = context.getBean(ModelMapper.class);

      // Assert that both references point to the same object (singleton scope)
      assertSame(firstInstance, secondInstance, "ModelMapper bean should be a singleton");
    } finally {
      // Always close the context to release resources
      context.close();
    }
  }

  /**
   * Tests correct ModelMapper default configuration settings
   */
  @Test
  public void testModelMapperDefaultConfiguration() {
    // Create an instance of WebConfig
    WebConfig webConfig = new WebConfig();

    // Get the ModelMapper
    ModelMapper modelMapper = webConfig.modelMapper();

    // Assert default configuration settings
    assertEquals(MatchingStrategies.STANDARD, modelMapper.getConfiguration().getMatchingStrategy(),
            "Default matching strategy should be STANDARD");
    assertEquals(AccessLevel.PUBLIC, modelMapper.getConfiguration().getFieldAccessLevel(),
            "Field access level should be PUBLIC by default");
    assertEquals(AccessLevel.PUBLIC, modelMapper.getConfiguration().getMethodAccessLevel(),
            "Method access level should be PUBLIC by default");
    assertFalse(modelMapper.getConfiguration().isFieldMatchingEnabled(),
            "Field matching should be disabled by default");
    assertFalse(modelMapper.getConfiguration().isAmbiguityIgnored(),
            "Ambiguity should not be ignored by default");
  }

  /**
   * Tests the ModelMapper functionality with a simple mapping scenario
   */
  @Test
  public void testModelMapperFunctionality() {
    // Create an instance of WebConfig
    WebConfig webConfig = new WebConfig();

    // Get the ModelMapper
    ModelMapper modelMapper = webConfig.modelMapper();

    // Create a source object
    Source source = new Source();
    source.setName("Test Name");
    source.setValue(42);

    // Map to destination object
    Destination destination = modelMapper.map(source, Destination.class);

    // Assert that mapping worked correctly
    assertNotNull(destination, "Mapped object should not be null");
    assertEquals(source.getName(), destination.getName(), "Name should be mapped correctly");
    assertEquals(source.getValue(), destination.getValue(), "Value should be mapped correctly");
  }

  /**
   * Test class for ModelMapper functionality test
   */
  private static class Source {
    private String name;
    private int value;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public int getValue() {
      return value;
    }

    public void setValue(int value) {
      this.value = value;
    }
  }

  /**
   * Test class for ModelMapper functionality test
   */
  private static class Destination {
    private String name;
    private int value;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public int getValue() {
      return value;
    }

    public void setValue(int value) {
      this.value = value;
    }
  }

  /**
   * Tests a custom configuration to ensure ModelMapper can be customized
   */
  @Test
  public void testCustomizedModelMapper() {
    // Create an instance of WebConfig and get the ModelMapper
    ModelMapper modelMapper = new WebConfig().modelMapper();

    // Customize the ModelMapper
    modelMapper.getConfiguration()
            .setMatchingStrategy(MatchingStrategies.STRICT)
            .setFieldMatchingEnabled(true)
            .setAmbiguityIgnored(true);

    // Assert that customization worked
    assertEquals(MatchingStrategies.STRICT, modelMapper.getConfiguration().getMatchingStrategy(),
            "Matching strategy should be updated to STRICT");
    assertTrue(modelMapper.getConfiguration().isFieldMatchingEnabled(),
            "Field matching should be enabled after customization");
    assertTrue(modelMapper.getConfiguration().isAmbiguityIgnored(),
            "Ambiguity should be ignored after customization");
  }

  /**
   * Tests more complex mapping with nested objects
   */
  @Test
  public void testNestedObjectMapping() {
    // Create an instance of WebConfig
    WebConfig webConfig = new WebConfig();

    // Get the ModelMapper
    ModelMapper modelMapper = webConfig.modelMapper();

    // Create nested source objects
    NestedSource nestedSource = new NestedSource();
    nestedSource.setDescription("Nested Description");

    ComplexSource complexSource = new ComplexSource();
    complexSource.setId(1);
    complexSource.setTitle("Complex Title");
    complexSource.setNested(nestedSource);

    // Map to destination object
    ComplexDestination complexDestination = modelMapper.map(complexSource, ComplexDestination.class);

    // Assert that mapping worked correctly
    assertNotNull(complexDestination, "Mapped complex object should not be null");
    assertEquals(complexSource.getId(), complexDestination.getId(), "ID should be mapped correctly");
    assertEquals(complexSource.getTitle(), complexDestination.getTitle(), "Title should be mapped correctly");

    // Assert that nested object was mapped correctly
    assertNotNull(complexDestination.getNested(), "Nested object should not be null");
    assertEquals(nestedSource.getDescription(), complexDestination.getNested().getDescription(),
            "Nested description should be mapped correctly");
  }

  /**
   * Test nested classes for complex mapping test
   */
  private static class NestedSource {
    private String description;

    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }
  }

  private static class NestedDestination {
    private String description;

    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }
  }

  private static class ComplexSource {
    private int id;
    private String title;
    private NestedSource nested;

    public int getId() {
      return id;
    }

    public void setId(int id) {
      this.id = id;
    }

    public String getTitle() {
      return title;
    }

    public void setTitle(String title) {
      this.title = title;
    }

    public NestedSource getNested() {
      return nested;
    }

    public void setNested(NestedSource nested) {
      this.nested = nested;
    }
  }

  private static class ComplexDestination {
    private int id;
    private String title;
    private NestedDestination nested;

    public int getId() {
      return id;
    }

    public void setId(int id) {
      this.id = id;
    }

    public String getTitle() {
      return title;
    }

    public void setTitle(String title) {
      this.title = title;
    }

    public NestedDestination getNested() {
      return nested;
    }

    public void setNested(NestedDestination nested) {
      this.nested = nested;
    }
  }
}