package io.crnk.gen.openapi.internal;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;

public enum OperationType {

  GET {
    @Override
    public PathItem merge(PathItem pathItem, Operation operation) {
      pathItem.setGet(OASMergeUtil.mergeOperations(operation, pathItem.getGet()));
      return pathItem;
    }
  },
  DELETE {
    @Override
    public PathItem merge(PathItem pathItem, Operation operation) {
      pathItem.setDelete(OASMergeUtil.mergeOperations(operation, pathItem.getDelete()));
      return pathItem;
    }
  },
  HEAD {
    @Override
    public PathItem merge(PathItem pathItem, Operation operation) {
      pathItem.setHead(OASMergeUtil.mergeOperations(operation, pathItem.getHead()));
      return pathItem;
    }
  },
  OPTIONS {
    @Override
    public PathItem merge(PathItem pathItem, Operation operation) {
      pathItem.setOptions(OASMergeUtil.mergeOperations(operation, pathItem.getOptions()));
      return pathItem;
    }
  },
  PATCH {
    @Override
    public PathItem merge(PathItem pathItem, Operation operation) {
      pathItem.setPatch(OASMergeUtil.mergeOperations(operation, pathItem.getPatch()));
      return pathItem;
    }
  },
  POST {
    @Override
    public PathItem merge(PathItem pathItem, Operation operation) {
      pathItem.setPost(OASMergeUtil.mergeOperations(operation, pathItem.getPost()));
      return pathItem;
    }
  },
  PUT {
    @Override
    public PathItem merge(PathItem pathItem, Operation operation) {
      pathItem.setPut(OASMergeUtil.mergeOperations(operation, pathItem.getPut()));
      return pathItem;
    }
  };

  public abstract PathItem merge(PathItem pathItem, Operation operation);

}
