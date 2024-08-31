package com.lemondead1.audit;

import com.lemondead1.audit.annotations.Audited;

import java.util.Map;

public interface Auditor {
  /**
   * Called when an audited method is called.
   *
   * @param type The event type as per {@linkplain Audited#value()}.
   * @param data Map of captured values with keys specified by {@linkplain Audited.Param#value()} or {@linkplain Audited.PresenceCheck#value()}.
   */
  void postEvent(String type, Map<String, Object> data);
}
