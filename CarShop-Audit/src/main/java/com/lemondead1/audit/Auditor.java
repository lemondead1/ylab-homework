package com.lemondead1.audit;

import java.util.Map;

public interface Auditor {
  void postEvent(String type, Map<String, Object> data);
}
