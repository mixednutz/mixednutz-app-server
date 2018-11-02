package net.mixednutz.app.server.controller;

@RepositoryRestController
public class ApiUserController {

  @ResquestMapping(value="/user", method=RequestMethod.POST)
  @ResponseStatus(HttpStatus.CREATED)
  public @ResponseBody PersistentEntityResource create(@RequestBody Resource<User> userRes,
    Authentication auth, PeristentEntityResourceAssembler resourceAssembler) {
    if (existing) {
      throw new Exception("Resource already exists");
    }
    return resourceAssembler.toResource(manager.save(user));
  }

}
