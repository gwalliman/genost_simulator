
package org.tempuri;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.tempuri package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _SetStatusState_QNAME = new QName("http://tempuri.org/", "state");
    private final static QName _GetStatusResponseGetStatusResult_QNAME = new QName("http://tempuri.org/", "GetStatusResult");
    private final static QName _SetCodeCode_QNAME = new QName("http://tempuri.org/", "code");
    private final static QName _GetCodeResponseGetCodeResult_QNAME = new QName("http://tempuri.org/", "GetCodeResult");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.tempuri
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link SetCodeResponse }
     * 
     */
    public SetCodeResponse createSetCodeResponse() {
        return new SetCodeResponse();
    }

    /**
     * Create an instance of {@link SetStatusResponse }
     * 
     */
    public SetStatusResponse createSetStatusResponse() {
        return new SetStatusResponse();
    }

    /**
     * Create an instance of {@link GetStatusResponse }
     * 
     */
    public GetStatusResponse createGetStatusResponse() {
        return new GetStatusResponse();
    }

    /**
     * Create an instance of {@link GetStatus }
     * 
     */
    public GetStatus createGetStatus() {
        return new GetStatus();
    }

    /**
     * Create an instance of {@link SetStatus }
     * 
     */
    public SetStatus createSetStatus() {
        return new SetStatus();
    }

    /**
     * Create an instance of {@link SetCode }
     * 
     */
    public SetCode createSetCode() {
        return new SetCode();
    }

    /**
     * Create an instance of {@link GetCodeResponse }
     * 
     */
    public GetCodeResponse createGetCodeResponse() {
        return new GetCodeResponse();
    }

    /**
     * Create an instance of {@link GetCode }
     * 
     */
    public GetCode createGetCode() {
        return new GetCode();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "state", scope = SetStatus.class)
    public JAXBElement<String> createSetStatusState(String value) {
        return new JAXBElement<String>(_SetStatusState_QNAME, String.class, SetStatus.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "GetStatusResult", scope = GetStatusResponse.class)
    public JAXBElement<String> createGetStatusResponseGetStatusResult(String value) {
        return new JAXBElement<String>(_GetStatusResponseGetStatusResult_QNAME, String.class, GetStatusResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "code", scope = SetCode.class)
    public JAXBElement<String> createSetCodeCode(String value) {
        return new JAXBElement<String>(_SetCodeCode_QNAME, String.class, SetCode.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "GetCodeResult", scope = GetCodeResponse.class)
    public JAXBElement<String> createGetCodeResponseGetCodeResult(String value) {
        return new JAXBElement<String>(_GetCodeResponseGetCodeResult_QNAME, String.class, GetCodeResponse.class, value);
    }

}
