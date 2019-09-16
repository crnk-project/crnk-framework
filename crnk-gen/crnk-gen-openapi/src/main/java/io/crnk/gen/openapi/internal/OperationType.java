package io.crnk.gen.openapi.internal;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;

public enum OperationType {
  GET {
    @Override
    public PathItem merge(PathItem pathItem, Operation operation) {
      pathItem.setGet(OASUtils.mergeOperations(operation, pathItem.getGet()));
      return pathItem;
    }
  },
  DELETE {
    @Override
    public PathItem merge(PathItem pathItem, Operation operation) {
      pathItem.setDelete(OASUtils.mergeOperations(operation, pathItem.getDelete()));
      return pathItem;
    }
  },
  HEAD {
    @Override
    public PathItem merge(PathItem pathItem, Operation operation) {
      pathItem.setHead(OASUtils.mergeOperations(operation, pathItem.getHead()));
      return pathItem;
    }
  },
  OPTIONS {
    @Override
    public PathItem merge(PathItem pathItem, Operation operation) {
      pathItem.setOptions(OASUtils.mergeOperations(operation, pathItem.getOptions()));
      return pathItem;
    }
  },
  PATCH {
    @Override
    public PathItem merge(PathItem pathItem, Operation operation) {
      pathItem.setPatch(OASUtils.mergeOperations(operation, pathItem.getPatch()));
      return pathItem;
    }
  },
  POST {
    @Override
    public PathItem merge(PathItem pathItem, Operation operation) {
      pathItem.setPost(OASUtils.mergeOperations(operation, pathItem.getPost()));
      return pathItem;
    }
  },
  PUT {
    @Override
    public PathItem merge(PathItem pathItem, Operation operation) {
      pathItem.setPut(OASUtils.mergeOperations(operation, pathItem.getPut()));
      return pathItem;
    }
  };

  public abstract PathItem merge(PathItem pathItem, Operation operation);

}
