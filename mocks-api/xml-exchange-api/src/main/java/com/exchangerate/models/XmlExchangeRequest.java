package com.exchangerate.models;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Solicitud para el proveedor de tipo de cambio con formato XML.
 * Utiliza estructura XML estándar para intercambio de datos
 * con el API Provider 2 especializado en servicios bancarios tradicionales.
 */
@XmlRootElement(name = "XML")
public class XmlExchangeRequest implements Serializable {
    
    private String from;
    private String to;
    private BigDecimal amount;
    
    // Constructor vacío requerido por JAXB
    public XmlExchangeRequest() {
    }
    
    // Constructor completo
    public XmlExchangeRequest(String from, String to, BigDecimal amount) {
        this.from = from;
        this.to = to;
        this.amount = amount;
    }
    
    @XmlElement(name = "From")
    public String getFrom() {
        return from;
    }
    
    public void setFrom(String from) {
        this.from = from;
    }
    
    @XmlElement(name = "To")
    public String getTo() {
        return to;
    }
    
    public void setTo(String to) {
        this.to = to;
    }
    
    @XmlElement(name = "Amount")
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}