//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v1.0.2-b15-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2006.07.11 at 02:47:38 CEST 
//


package de.thorstenberger.taskmodel.complex.jaxb;


/**
 * Java content class for anonymous complex type.
 * <p>The following schema fragment specifies the expected content contained within this java content object. (defined at file:/D:/java_files/eclipse/workspace/elatePortal/taskmodel-core/jaxb/complexTaskDef.xsd line 419)
 * <p>
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="problem" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="images" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="mutableTemplateImage" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="immutableBackgroundImage" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="correctionTemplateImage" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="textualAnswer" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="textFieldWidth" minOccurs="0">
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}int">
 *                       &lt;minInclusive value="1"/>
 *                     &lt;/restriction>
 *                   &lt;/element>
 *                   &lt;element name="textFieldHeight" minOccurs="0">
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}int">
 *                       &lt;minInclusive value="1"/>
 *                     &lt;/restriction>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="hint" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="colorChangeable" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="strokewidthChangeable" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="correctionHint" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 */
public interface PaintSubTaskDefType {


    /**
     * 
     * @return
     *     possible object is
     *     {@link de.thorstenberger.taskmodel.complex.jaxb.PaintSubTaskDefType.ImagesType}
     */
    de.thorstenberger.taskmodel.complex.jaxb.PaintSubTaskDefType.ImagesType getImages();

    /**
     * 
     * @param value
     *     allowed object is
     *     {@link de.thorstenberger.taskmodel.complex.jaxb.PaintSubTaskDefType.ImagesType}
     */
    void setImages(de.thorstenberger.taskmodel.complex.jaxb.PaintSubTaskDefType.ImagesType value);

    boolean isSetImages();

    void unsetImages();

    /**
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String}
     */
    java.lang.String getCorrectionHint();

    /**
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String}
     */
    void setCorrectionHint(java.lang.String value);

    boolean isSetCorrectionHint();

    void unsetCorrectionHint();

    /**
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String}
     */
    java.lang.String getHint();

    /**
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String}
     */
    void setHint(java.lang.String value);

    boolean isSetHint();

    void unsetHint();

    /**
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String}
     */
    java.lang.String getProblem();

    /**
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String}
     */
    void setProblem(java.lang.String value);

    boolean isSetProblem();

    void unsetProblem();

    /**
     * 
     */
    boolean isColorChangeable();

    /**
     * 
     */
    void setColorChangeable(boolean value);

    boolean isSetColorChangeable();

    void unsetColorChangeable();

    /**
     * 
     */
    boolean isStrokewidthChangeable();

    /**
     * 
     */
    void setStrokewidthChangeable(boolean value);

    boolean isSetStrokewidthChangeable();

    void unsetStrokewidthChangeable();

    /**
     * 
     * @return
     *     possible object is
     *     {@link de.thorstenberger.taskmodel.complex.jaxb.PaintSubTaskDefType.TextualAnswerType}
     */
    de.thorstenberger.taskmodel.complex.jaxb.PaintSubTaskDefType.TextualAnswerType getTextualAnswer();

    /**
     * 
     * @param value
     *     allowed object is
     *     {@link de.thorstenberger.taskmodel.complex.jaxb.PaintSubTaskDefType.TextualAnswerType}
     */
    void setTextualAnswer(de.thorstenberger.taskmodel.complex.jaxb.PaintSubTaskDefType.TextualAnswerType value);

    boolean isSetTextualAnswer();

    void unsetTextualAnswer();

    /**
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String}
     */
    java.lang.String getId();

    /**
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String}
     */
    void setId(java.lang.String value);

    boolean isSetId();

    void unsetId();


    /**
     * Java content class for anonymous complex type.
     * <p>The following schema fragment specifies the expected content contained within this java content object. (defined at file:/D:/java_files/eclipse/workspace/elatePortal/taskmodel-core/jaxb/complexTaskDef.xsd line 423)
     * <p>
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="mutableTemplateImage" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="immutableBackgroundImage" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="correctionTemplateImage" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     */
    public interface ImagesType {


        /**
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String}
         */
        java.lang.String getImmutableBackgroundImage();

        /**
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String}
         */
        void setImmutableBackgroundImage(java.lang.String value);

        boolean isSetImmutableBackgroundImage();

        void unsetImmutableBackgroundImage();

        /**
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String}
         */
        java.lang.String getCorrectionTemplateImage();

        /**
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String}
         */
        void setCorrectionTemplateImage(java.lang.String value);

        boolean isSetCorrectionTemplateImage();

        void unsetCorrectionTemplateImage();

        /**
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String}
         */
        java.lang.String getMutableTemplateImage();

        /**
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String}
         */
        void setMutableTemplateImage(java.lang.String value);

        boolean isSetMutableTemplateImage();

        void unsetMutableTemplateImage();

    }


    /**
     * Java content class for anonymous complex type.
     * <p>The following schema fragment specifies the expected content contained within this java content object. (defined at file:/D:/java_files/eclipse/workspace/elatePortal/taskmodel-core/jaxb/complexTaskDef.xsd line 432)
     * <p>
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="textFieldWidth" minOccurs="0">
     *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}int">
     *             &lt;minInclusive value="1"/>
     *           &lt;/restriction>
     *         &lt;/element>
     *         &lt;element name="textFieldHeight" minOccurs="0">
     *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}int">
     *             &lt;minInclusive value="1"/>
     *           &lt;/restriction>
     *         &lt;/element>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     */
    public interface TextualAnswerType {


        /**
         * 
         */
        int getTextFieldWidth();

        /**
         * 
         */
        void setTextFieldWidth(int value);

        boolean isSetTextFieldWidth();

        void unsetTextFieldWidth();

        /**
         * 
         */
        int getTextFieldHeight();

        /**
         * 
         */
        void setTextFieldHeight(int value);

        boolean isSetTextFieldHeight();

        void unsetTextFieldHeight();

    }

}
