package net.mixednutz.app.server;

@Configuration
public class RestConfig {

  @Bean
  public ResouceProcessor<Resource<User>> userProcessor() {
    return new ResourceProcessor<Resource<User>>() {
      public Resource<User> process(Resource<User> resource) {
        //TODO add actions
        //Link link = linkTo(methodOn(Controller.class)
        //  .controllerMethod();
        //resource.add(link);
        //return resource;
      }
    };
  }


}
