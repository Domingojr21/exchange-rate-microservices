package com.exchangerate.models;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Respuesta del proveedor de tipo de cambio con formato XML.
 * Retorna el resultado de la conversión en formato XML estándar
 * desde el API Provider 2.
 */
@XmlRootElement(name = "XML")
public class XmlExchangeResponse implements Serializable {
    
    private BigDecimal result;
    
    // Constructor vacío requerido por JAXB
    public XmlExchangeResponse() {
    }
    
    // Constructor con resultado
    public XmlExchangeResponse(BigDecimal result) {
        this.result = result;
    }
    
    @XmlElement(name = "Result")
    public BigDecimal getResult() {
        return result;
    }
    
    public void setResult(BigDecimal result) {
        this.result = result;
    }
}