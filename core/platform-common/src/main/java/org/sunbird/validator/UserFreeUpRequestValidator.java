package org.sunbird.validator;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.sunbird.exception.ProjectCommonException;
import org.sunbird.exception.ResponseCode;
import org.sunbird.keys.JsonKey;
import org.sunbird.request.Request;
import org.sunbird.util.ProjectUtil;

public class UserFreeUpRequestValidator extends BaseRequestValidator {

  private Request request;
  private static List<String> identifiers = new ArrayList<>();

  static {
    identifiers.add(JsonKey.EMAIL);
    identifiers.add(JsonKey.PHONE);
  }

  private static final int ERROR_CODE = ResponseCode.CLIENT_ERROR.getResponseCode();

  /**
   * this method is used to get the instance to UserFreeUpRequestValidator class
   *
   * @param request
   * @return
   */
  public static UserFreeUpRequestValidator getInstance(Request request) {
    return new UserFreeUpRequestValidator(request);
  }

  private UserFreeUpRequestValidator(Request request) {
    this.request = request;
  }

  /** this is the method we need to call to validate the IdentifierFreeUpUser request. */
  public void validate() {
    validateIdPresence();
    validateIdentifier();
  }

  private void validateIdPresence() {
    validateParam(
        (String) request.getRequest().get(JsonKey.ID),
        ResponseCode.mandatoryParamsMissing,
        JsonKey.ID);
  }

  private void validateIdentifier() {
    validatePresence();
    validateObject();
    validateSubset();
  }

  private void validatePresence() {
    if (!request.getRequest().containsKey(JsonKey.IDENTIFIER)) {
      throw new ProjectCommonException(
          ResponseCode.mandatoryParamsMissing,
          MessageFormat.format(
              ResponseCode.mandatoryParamsMissing.getErrorMessage(), JsonKey.IDENTIFIER),
          ResponseCode.CLIENT_ERROR.getResponseCode());
    }
  }

  private void validateObject() {
    Object identifierType = request.getRequest().get(JsonKey.IDENTIFIER);
    if (!(identifierType instanceof List)) {
      throw new ProjectCommonException(
          ResponseCode.dataTypeError,
          ProjectUtil.formatMessage(
              ResponseCode.dataTypeError.getErrorMessage(), JsonKey.IDENTIFIER, JsonKey.LIST),
          ERROR_CODE);
    }
  }

  private void validateSubset() {
    List<String> identifierVal = (List<String>) request.getRequest().get(JsonKey.IDENTIFIER);
    if (!identifiers.containsAll(identifierVal)) {
      throw new ProjectCommonException(
          ResponseCode.dataTypeError,
          ProjectUtil.formatMessage(
              String.format(
                  "%s %s",
                  ResponseCode.invalidIdentifier.getErrorMessage(),
                  Arrays.toString(identifiers.toArray())),
              JsonKey.IDENTIFIER,
              JsonKey.DATA),
          ERROR_CODE);
    }
  }
}
