/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package si.rso.bicycle.schemas;
@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public enum NotificationType {
  EMAIL, SMS, PUSH  ;
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"enum\",\"name\":\"NotificationType\",\"namespace\":\"si.rso.bicycle.schemas\",\"symbols\":[\"EMAIL\",\"SMS\",\"PUSH\"]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
}
