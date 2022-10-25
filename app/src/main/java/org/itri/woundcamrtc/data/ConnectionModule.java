package org.itri.woundcamrtc.data;


public class ConnectionModule {

  private boolean success = false;
  private String errorMessage = "No error";

  public ConnectionModule() {
  }

  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}
