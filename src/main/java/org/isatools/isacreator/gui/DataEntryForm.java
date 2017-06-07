/**
 ISAcreator is a component of the ISA software suite (http://www.isa-tools.org)

 License:
 ISAcreator is licensed under the Common Public Attribution License version 1.0 (CPAL)

 EXHIBIT A. CPAL version 1.0
 The contents of this file are subject to the CPAL version 1.0 (the License);
 you may not use this file except in compliance with the License. You may obtain a
 copy of the License at http://isa-tools.org/licenses/ISAcreator-license.html.
 The License is based on the Mozilla Public License version 1.1 but Sections
 14 and 15 have been added to cover use of software over a computer network and
 provide for limited attribution for the Original Developer. In addition, Exhibit
 A has been modified to be consistent with Exhibit B.

 Software distributed under the License is distributed on an AS IS basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 the specific language governing rights and limitations under the License.

 The Original Code is ISAcreator.
 The Original Developer is the Initial Developer. The Initial Developer of the
 Original Code is the ISA Team (Eamonn Maguire, eamonnmag@gmail.com;
 Philippe Rocca-Serra, proccaserra@gmail.com; Susanna-Assunta Sansone, sa.sanson@gmail.com;
 http://www.isa-tools.org). All portions of the code written by the ISA Team are
 Copyright (c) 2007-2011 ISA Team. All Rights Reserved.

 EXHIBIT B. Attribution Information
 Attribution Copyright Notice: Copyright (c) 2008-2011 ISA Team
 Attribution Phrase: Developed by the ISA Team
 Attribution URL: http://www.isa-tools.org
 Graphic Image provided in the Covered Code as file: http://isa-tools.org/licenses/icons/poweredByISAtools.png
 Display of Attribution Information is required in Larger Works which are defined in the CPAL as a work which combines Covered Code or portions thereof with code not governed by the terms of the CPAL.

 Sponsors:
 The ISA Team and the ISA software suite have been funded by the EU Carcinogenomics project (http://www.carcinogenomics.eu), the UK BBSRC (http://www.bbsrc.ac.uk), the UK NERC-NEBC (http://nebc.nerc.ac.uk) and in part by the EU NuGO consortium (http://www.nugo.org/everyone).
 */

package org.isatools.isacreator.gui;

import com.explodingpixels.macwidgets.IAppWidgetFactory;
import org.apache.commons.collections15.OrderedMap;
import org.apache.commons.collections15.map.ListOrderedMap;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.isatools.isacreator.calendar.CalendarGUI;
import org.isatools.isacreator.common.DropDownComponent;
import org.isatools.isacreator.common.TextEditUtility;
import org.isatools.isacreator.common.UIHelper;
import org.isatools.isacreator.configuration.DataTypes;
import org.isatools.isacreator.configuration.FieldObject;
import org.isatools.isacreator.configuration.RecommendedOntology;
import org.isatools.isacreator.effects.components.RoundedJTextField;
import org.isatools.isacreator.filechooser.FileChooserUI;
import org.isatools.isacreator.gui.formelements.FieldTypes;
import org.isatools.isacreator.gui.formelements.SubForm;
import org.isatools.isacreator.gui.formelements.SubFormField;
import org.isatools.isacreator.gui.listeners.propertychange.DateChangedCancelledEvent;
import org.isatools.isacreator.gui.listeners.propertychange.DateChangedEvent;
import org.isatools.isacreator.gui.listeners.propertychange.OntologySelectedEvent;
import org.isatools.isacreator.gui.listeners.propertychange.OntologySelectionCancelledEvent;
import org.isatools.isacreator.gui.reference.DataEntryReferenceObject;
import org.isatools.isacreator.io.importisa.investigationproperties.InvestigationFileSection;
import org.isatools.isacreator.managers.ApplicationManager;
import org.isatools.isacreator.model.*;
import org.isatools.isacreator.ontologyselectiontool.OntologySelectionTool;
import org.isatools.isacreator.utils.StringProcessing;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 *
 *
 */
public class DataEntryForm extends JLayeredPane implements Serializable {

    public static final String META_OPENING_TAG = "[[";
    public static final String META_CLOSING_TAG = "]]";
    public static final String META_TAG_DELIMITER = "|";
    public static final String META_KEY_DELIMITER = "=";
    public static final String META_KEY_TYPE = "type";
    public static final String META_KEY_FORMAT = "format";

    public static final int SUBFORM_WIDTH = 300;

    private DataEntryEnvironment dataEntryEnvironment;
    protected Map<FieldTypes, JPanel> fieldTypeToFieldContainer;
    protected Map<FieldTypes, SubForm> fieldTypeToSubform;


    // this will house the translation between Comment aliases e.g. Publication Journal [c] to Comment[Publication Journal]
    protected Map<String, String> aliasesToRealNames;
    protected Map<String, String> realNamesToAliases;


    protected OrderedMap<String, JComponent> fieldDefinitions;

    public DataEntryForm() {
        this(null);
    }

    public DataEntryForm(DataEntryEnvironment dataEntryEnvironment) {
        this.dataEntryEnvironment = dataEntryEnvironment;
        fieldTypeToFieldContainer = new HashMap<FieldTypes, JPanel>();
        fieldTypeToSubform = new HashMap<FieldTypes, SubForm>();
    }

    public void update() {
        // implemented in subclasses
    }

    public JComponent createDateDropDown(JTextComponent field) {

        CalendarGUI calendar = new CalendarGUI();
        calendar.createGUI();

        DropDownComponent dropdown = new DropDownComponent(field, calendar, DropDownComponent.CALENDAR);

        calendar.addPropertyChangeListener("selectedDate",
                new DateChangedEvent(calendar, dropdown, field));
        calendar.addPropertyChangeListener("noneSelected",
                new DateChangedCancelledEvent(calendar, dropdown));

        return dropdown;
    }

    /**
     * Create a field panel which has a grid layout, and is opaque
     *
     * @param rows    - Number of rows to be added for the field
     * @param columns - Number of columns to be added
     * @return JPanel with a GridLayout with the rows and columns specified
     */
    public JPanel createFieldPanel(int rows, int columns) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(rows, columns));
        panel.setOpaque(false);
        return panel;
    }

    public void setDataEntryEnvironment(DataEntryEnvironment dataEntryEnvironment) {
        this.dataEntryEnvironment = dataEntryEnvironment;
    }

    public JPanel getContainerForFieldType(FieldTypes type) {
        return fieldTypeToFieldContainer.get(type);
    }

    public SubForm getSubFormForFieldType(FieldTypes type) {
        return fieldTypeToSubform.get(type);
    }

    public void setSubFormForFieldType(FieldTypes type, SubForm subform) {
        fieldTypeToSubform.put(type, subform);
    }


    /**
     * Method to be overridden by subclasses for creating all fields
     */
    public void createFields() {
    }

    public JLabel createLabel(String labelName) {
        JLabel label = UIHelper.createLabel(labelName, UIHelper.VER_11_BOLD);
        label.setVerticalAlignment(JLabel.TOP);
        label.setHorizontalAlignment(JLabel.LEFT);

        return label;
    }

    public JComponent createOntologyDropDown(String fieldName, JTextComponent field,
                                             boolean allowsMultiple, boolean forceOntology, Map<String, RecommendedOntology> recommendedOntologySource) {


        OntologySelectionTool ontologySelectionTool = new OntologySelectionTool(allowsMultiple, forceOntology, recommendedOntologySource);
        ontologySelectionTool.createGUI();

        DropDownComponent dropdown = new DropDownComponent(fieldName, field, ontologySelectionTool, DropDownComponent.ONTOLOGY);

        ontologySelectionTool.addPropertyChangeListener("selectedOntology", new OntologySelectedEvent(ontologySelectionTool, dropdown, field));

        ontologySelectionTool.addPropertyChangeListener("noSelectedOntology", new OntologySelectionCancelledEvent(ontologySelectionTool, dropdown));

        return dropdown;
    }

    public JComponent createFileField(final JTextComponent field) {
        final FileChooserUI fileChooserUI = new FileChooserUI();
        final DropDownComponent dropdown = new DropDownComponent(field, fileChooserUI, DropDownComponent.FILE);

        fileChooserUI.addPropertyChangeListener("selectedFiles", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                String contents = "";
                int count = 0;

                for (String file : fileChooserUI.getSelectedFiles()) {
                    contents += file;
                    if (count != fileChooserUI.getSelectedFiles().length - 1) {
                        contents += ",";
                    }
                    count++;
                }
                field.setText(contents);
                dropdown.hidePopup(fileChooserUI);
            }
        });

        fileChooserUI.addPropertyChangeListener("noSelectedFiles", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                dropdown.hidePopup(fileChooserUI);
            }
        });

        return dropdown;

    }

    protected JPanel createTextEditEnabledField(JTextComponent component) {
        JPanel container = new JPanel();
        container.setLayout(new BorderLayout());

        container.add(component, BorderLayout.CENTER);

        TextEditUtility textEdit = new TextEditUtility(component);
        textEdit.setVerticalAlignment(SwingConstants.TOP);

        JPanel textEditPanel = new JPanel();
        UIHelper.setLayoutForEditingIcons(textEditPanel, textEdit);

        textEditPanel.setSize(new Dimension(23, 23));

        container.add(textEditPanel, BorderLayout.EAST);

        return container;
    }


    /**
     * Final step in setting up the JLayeredPane
     */
    protected void finalisePane() {
        setVisible(true);
    }

    public DataEntryEnvironment getDataEntryEnvironment() {
        return dataEntryEnvironment;
    }

    /**
     * Generic initialisers for frame
     */
    protected void instantiatePane() {
        setLayout(new BorderLayout());
        setBackground(UIHelper.BG_COLOR);
        setPreferredSize(new Dimension(600, 600));
        setBorder(BorderFactory.createLineBorder(UIHelper.LIGHT_GREEN_COLOR));
    }

    public Map<String, Assay> getAssays() {
        return null;
    }

    public List<Factor> getFactors() {
        return null;
    }

    public List<Contact> getContacts() {
        return null;
    }

    public List<Protocol> getProtocols() {
        return null;
    }

    public Study getStudy() {
        return null;
    }

    public Investigation getInvestigation() {
        return null;
    }

    public void removeAssay(String ref) {

    }

    public List<Publication> getPublications() {
        return null;
    }

    public List<StudyDesign> getDesigns() {
        return null;
    }


    public int translateDataTypeToSubFormFieldType(DataTypes dataType, boolean acceptsMultipleValues) {
        if (dataType == DataTypes.ONTOLOGY_TERM) {
            return acceptsMultipleValues ? SubFormField.MULTIPLE_ONTOLOGY_SELECT : SubFormField.SINGLE_ONTOLOGY_SELECT;
        }

        if (dataType == DataTypes.DATE) {
            return SubFormField.DATE;
        }

        if (dataType == DataTypes.LONG_STRING) {
            return SubFormField.LONG_STRING;
        }

        if (dataType == DataTypes.LIST) {
            return SubFormField.COMBOLIST;
        }

        return SubFormField.STRING;

    }

    public void generateAliases(Set<String> fieldValues) {

        if (aliasesToRealNames == null) {
            aliasesToRealNames = new HashMap<String, String>();
            realNamesToAliases = new HashMap<String, String>();
        }


        for (String fieldName : fieldValues) {

            if (fieldName.toLowerCase().startsWith("comment")) {
                String alias = StringProcessing.extractQualifierFromField(fieldName) + " [c]";
                aliasesToRealNames.put(alias, fieldName);
                realNamesToAliases.put(fieldName, alias);
            }
        }
    }

    /**
     * Adds the fields describing a section to a Container (e.g. JPanel, Box, etc.)
     *
     * @param containerToAddTo @see JPanel, @see Box, @see  Container
     * @param sectionToAddTo   - @see InvestigationFileSection as a reference for where the fields are being added
     * @param fieldValues      - Map of field name to values.
     * @param referenceObject  - A @see DataEntryReferenceObject which gives information about
     */
    public void addFieldsToPanel(Container containerToAddTo, InvestigationFileSection sectionToAddTo, OrderedMap<String, String> fieldValues, DataEntryReferenceObject referenceObject) {


        if (fieldDefinitions == null) {
            fieldDefinitions = new ListOrderedMap<String, JComponent>();
        }

        Set<String> ontologyFields = referenceObject.getOntologyTerms(sectionToAddTo);
        Set<String> fieldsToIgnore = referenceObject.getFieldsToIgnore();

        final Map<String, JComponent> fields = new HashMap<String, JComponent>();

        for (String fieldName : fieldValues.keySet()) {

            if (!fieldsToIgnore.contains(fieldName)) {
                FieldObject fieldDescriptor = referenceObject.getFieldDefinition(fieldName);

                if (!fieldDescriptor.isHidden()) {
                    String tmpFieldName = fieldName;

                    if (realNamesToAliases.containsKey(fieldName)) {
                        tmpFieldName = realNamesToAliases.get(fieldName);
                    }

                    JPanel fieldPanel = createFieldPanel(1, 2);
                    JLabel fieldLabel = createLabel(tmpFieldName);

                    JComponent textComponent;

                    if (fieldDescriptor.getDatatype() == DataTypes.STRING || fieldDescriptor.getDatatype() == DataTypes.ONTOLOGY_TERM || fieldDescriptor.getDatatype() == DataTypes.DATE) {
                        textComponent = new RoundedJTextField(10);
                    } else if (fieldDescriptor.getDatatype() == DataTypes.LONG_STRING) {
                        textComponent = new JTextArea();

                        ((JTextArea) textComponent).setSelectionColor(UIHelper.LIGHT_GREEN_COLOR);
                        ((JTextArea) textComponent).setSelectedTextColor(UIHelper.BG_COLOR);

                        ((JTextArea) textComponent).setWrapStyleWord(true);
                        ((JTextArea) textComponent).setLineWrap(true);
                        textComponent.setBackground(UIHelper.BG_COLOR);
                        textComponent.setBorder(UIHelper.GREEN_ROUNDED_BORDER);
                    } else if (fieldDescriptor.getDatatype() == DataTypes.LIST) {

                        final Map<String, Map<String, String>> linkds = new HashMap<String, Map<String, String>>();

                        for (int i = 0; i < fieldDescriptor.getFieldList().length; i++) {
                            final String item = StringUtils.trim(StringUtils.substringBefore(
                                    fieldDescriptor.getFieldList()[i], META_OPENING_TAG));
                            linkds.put(item, getMetaKeyValuePairs(fieldDescriptor.getFieldList()[i]));
                            // update list item
                            fieldDescriptor.getFieldList()[i] = item;
                        }

                        textComponent = new JComboBox(fieldDescriptor.getFieldList());
                        // find and select value in combo box
                        int index = ArrayUtils.indexOf(fieldDescriptor.getFieldList(), fieldValues.get(fieldName));
                        if (index > -1) {
                            ((JComboBox) textComponent).setSelectedIndex(index);
                        }

                        ((JComboBox) textComponent).addItemListener(new ItemListener() {
                            public void itemStateChanged(ItemEvent e) {
                                if (e.getStateChange() == ItemEvent.DESELECTED) return;

                                Map<String, String> fieldsMap = linkds.get(e.getItem());
                                for (String field : fieldsMap.keySet()) {
                                    JComboBox component = (JComboBox) fields.get(field);
                                    if (component != null) component.setSelectedItem(fieldsMap.get(field));
                                }
                            }
                        });
                    } else {
                        textComponent = new RoundedJTextField(10);
                    }

                    final Map<String, String> metadata = getMetaKeyValuePairs(fieldDescriptor.getDescription());
                    textComponent.setInputVerifier(new CustomDateInputVerifier(metadata));

                    // collect all fields (in order to updated 'linked' values)
                    fields.put(fieldName, textComponent);

                    if (textComponent instanceof JTextArea || textComponent instanceof JTextComponent) {
                        ((JTextComponent) textComponent).setText(fieldValues.get(fieldName).equals("")
                                ? fieldDescriptor.getDefaultVal() : fieldValues.get(fieldName));
                    }
                    textComponent.setToolTipText(fieldDescriptor.getDescription());

                    UIHelper.renderComponent(textComponent, UIHelper.VER_11_PLAIN, UIHelper.DARK_GREEN_COLOR, false);

                    fieldPanel.add(fieldLabel);

                    if (textComponent instanceof JTextArea) {

                        JScrollPane invDescScroll = new JScrollPane(textComponent,
                                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                        invDescScroll.setPreferredSize(new Dimension(200, 75));

                        invDescScroll.getViewport().setBackground(UIHelper.BG_COLOR);

                        ((JTextArea) textComponent).getDocument().addDocumentListener(new DocumentChangeListener());

                        IAppWidgetFactory.makeIAppScrollPane(invDescScroll);
                        fieldPanel.add(UIHelper.createTextEditEnableJTextArea(invDescScroll, (JTextArea) textComponent));
                    } else if (textComponent instanceof JTextComponent) {

                        if (fieldDescriptor.getDatatype() == DataTypes.ONTOLOGY_TERM || ontologyFields.contains(fieldName)) {
                            fieldPanel.add(createOntologyDropDown(fieldName, (JTextComponent) textComponent, true, false, fieldDescriptor.getRecommmendedOntologySource()));
                        } else if (fieldDescriptor.getDatatype() == DataTypes.DATE) {
                            fieldPanel.add(createDateDropDown((JTextComponent) textComponent));
                        } else if (fieldDescriptor.isAcceptsFileLocations()) {
                            fieldPanel.add(createFileField((JTextComponent) textComponent));
                        } else {
                            fieldPanel.add(textComponent);
                        }
                        ((JTextComponent) textComponent).getDocument().addDocumentListener(new DocumentChangeListener());
                    } else {
                        fieldPanel.add(textComponent);
                    }




                    fieldDefinitions.put(tmpFieldName, textComponent);

                    containerToAddTo.add(fieldPanel);
                    containerToAddTo.add(Box.createVerticalStrut(5));
                }
            }
        }
    }

    /**
     * Returns map of meta descriptors
     *
     * @param desc string to be processed
     * @return metadata
     */
    private HashMap<String, String> getMetaKeyValuePairs(final String desc) {
        final HashMap<String, String> metadata = new HashMap<String, String>();

        final String conf = StringUtils.substringBetween(desc, META_OPENING_TAG, META_CLOSING_TAG);
        if (null == conf) {
            return metadata;
        }

        for (String pair : StringUtils.split(conf, META_TAG_DELIMITER)) {
            String[] dat = StringUtils.split(pair, META_KEY_DELIMITER);
            metadata.put(dat[0], dat[1]);
        }

        return metadata;
    }

    /**
     * Custom InputVerifier for fields of date type
     */
    class CustomDateInputVerifier extends InputVerifier {

        final Map<String, String> conf;

        CustomDateInputVerifier(Map<String, String> conf) {
            this.conf = conf;
        }

        @Override
        public boolean verify(final JComponent input) {

            if (this.conf.isEmpty()) {
                return true;
            }

            final RoundedJTextField field = (RoundedJTextField) input;

            if (StringUtils.isEmpty(field.getText())) {
                field.unsetWarningMode();
                return true;
            }
            if ("date".equals(this.conf.get(META_KEY_TYPE))) {
                final Date date = parseDate(field.getText(), getPredefinedDateFormats());

                if (null == date) {
                    field.setWarningMode();
                } else {
                    field.unsetWarningMode();
                    updateFieldText(field, date);
                }
                return date != null;
            }

            return false;
        }

        /**
         * Returns predefined date formats
         *
         * @return
         */
        private List<SimpleDateFormat> getPredefinedDateFormats() {
            final List<SimpleDateFormat> formats = new ArrayList<SimpleDateFormat>();
            formats.add(new SimpleDateFormat("MM-dd-yyyy"));
            formats.add(new SimpleDateFormat("yyyyMMdd"));

            final String format = this.conf.get(META_KEY_FORMAT);
            if (StringUtils.isNotEmpty(format)) formats.add(new SimpleDateFormat(format));

            return formats;
        }

        /**
         * Parses text into date
         *
         * @param text text to be processed
         * @param formats date formats
         * @return date or null
         */
        private Date parseDate(final String text, final List<SimpleDateFormat> formats) {
            for (final SimpleDateFormat format : formats) {
                final Date date = parseDate(text, format);
                if (date != null) return date;
            }
            return null;
        }

        /**
         * Parses text into date
         *
         * @param text text to be processed
         * @param format date format
         * @return date or null
         */
        private Date parseDate(final String text, final SimpleDateFormat format) {
            try {
                final Date date = format.parse(text);
                if (text.equals(format.format(date))) {
                    return date;
                } else {
                    return null;
                }
            } catch (final ParseException e) {
                // cannot parse date, add warning
                return null;
            }
        }

        /**
         * Formats date (according to configuration) and updates field value
         *
         * @param field text field
         * @param date date
         */
        private void updateFieldText(final JTextField field, final Date date) {
            final String format = this.conf.get(META_KEY_FORMAT);
            if (StringUtils.isNotEmpty(format)) {
                field.setText(new SimpleDateFormat(format).format(date));
            }
        }
    }

    protected SubFormField generateSubFormField(Set<String> fieldsToIgnore, Set<String> ontologyFields, Study study, String fieldName) {
        FieldObject fieldDescriptor = study.getReferenceObject().getFieldDefinition(fieldName);
        return createField(fieldsToIgnore, ontologyFields, fieldDescriptor, fieldName);
    }

    protected SubFormField generateSubFormField(Set<String> fieldsToIgnore, Set<String> ontologyFields, Investigation investigation, String fieldName) {
        FieldObject fieldDescriptor = investigation.getReferenceObject().getFieldDefinition(fieldName);
        return createField(fieldsToIgnore, ontologyFields, fieldDescriptor, fieldName);

    }

    private SubFormField createField(Set<String> fieldsToIgnore, Set<String> ontologyFields, FieldObject fieldDescriptor, String fieldName) {
        if (!fieldsToIgnore.contains(fieldName)) {

            int fieldType = SubFormField.STRING;

            boolean matchingOntologyDataType = true;
            if (fieldDescriptor != null) {
                matchingOntologyDataType = fieldDescriptor.getDatatype() == DataTypes.ONTOLOGY_TERM;
            }

            if (ontologyFields.contains(fieldName) && matchingOntologyDataType) {
                fieldType = SubFormField.SINGLE_ONTOLOGY_SELECT;

                if (fieldDescriptor != null) {
                    if (fieldDescriptor.isAcceptsMultipleValues()) {
                        fieldType = SubFormField.MULTIPLE_ONTOLOGY_SELECT;
                    }
                }
                return new SubFormField(fieldName, fieldType, fieldDescriptor.getRecommmendedOntologySource());

            } else {

                if (fieldDescriptor != null) {
                    fieldType = translateDataTypeToSubFormFieldType(fieldDescriptor.getDatatype(),
                            fieldDescriptor.isAcceptsMultipleValues());

                    if (fieldType == SubFormField.STRING) {
                        if (fieldDescriptor.isAcceptsFileLocations()) {
                            fieldType = SubFormField.FILE;
                        }
                    }

                    if (fieldType == SubFormField.SINGLE_ONTOLOGY_SELECT || fieldType == SubFormField.MULTIPLE_ONTOLOGY_SELECT) {
                        return new SubFormField(fieldName, fieldType, fieldDescriptor.getRecommmendedOntologySource());
                    }
                }

                if (fieldType == SubFormField.COMBOLIST) {
                    return new SubFormField(fieldName, fieldType, fieldDescriptor.getFieldList());
                } else {
                    return new SubFormField(fieldName, fieldType);
                }

            }
        }

        return null;
    }

    /**
     * Given a list of ISASections, it returns them as a String
     *
     * @param sectionTitle    a string that is the section title
     * @param sectionToOutput a list of objects that are subclasses of ISASection
     * @return a string with the serialization of the ISASection
     */
    protected String getISASectionAsString(String sectionTitle, List<? extends ISASection> sectionToOutput) {
        StringBuilder representation = new StringBuilder();
        representation.append(sectionTitle.toUpperCase().trim()).append("\n");


        if (sectionToOutput.size() > 0) {
            for (String fieldName : sectionToOutput.get(0).getFieldValues().keySet()) {
                representation.append(fieldName);
                if (sectionToOutput.size() > 0) {
                    representation.append("\t");
                }
                // now add the field values in
                int count = 0;
                for (ISASection section : sectionToOutput) {
                    String value = section.getFieldValues().get(fieldName);
                    representation.append(value);
                    if (count != sectionToOutput.size() - 1) {
                        representation.append("\t");
                    }
                    count++;
                }
                representation.append("\n");
            }
        }
        return representation.toString();
    }

    /**
     * This listener sets an ISATab to modified if changes have been made to its contents.
     */
    class DocumentChangeListener implements DocumentListener {

        public void insertUpdate(DocumentEvent documentEvent) {
            ApplicationManager.setModified(true);
        }

        public void removeUpdate(DocumentEvent documentEvent) {
            ApplicationManager.setModified(true);
        }

        public void changedUpdate(DocumentEvent documentEvent) {
            ApplicationManager.setModified(true);
        }
    }

    public int estimateSubformHeight(int numberOfFields) {
        // assuming that the option height, table header height and field height are all similar.
        int sectionHeight = 18;
        return (sectionHeight * 2) + (numberOfFields * sectionHeight);
    }


}
