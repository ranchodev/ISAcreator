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

package org.isatools.isacreator.ontologycombo;

import org.isatools.isacreator.ontologymanager.common.OntologyTerm;

import javax.swing.*;
import java.awt.*;

/**
 * Provides a Cell Renderer so that a OntologyCombo can display data from multiple data.
 *
 */
public class OntologyComboCellRenderer<E extends OntologyTerm> implements ListCellRenderer<OntologyTerm> {

    private final JLabel labelL = new JLabel() {
        @Override public void updateUI() {
            super.updateUI();
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
        }
    };

    private final JLabel labelR = new JLabel() {
        @Override public void updateUI() {
            super.updateUI();
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
            setForeground(Color.GRAY);
            setHorizontalAlignment(SwingConstants.RIGHT);
        }
        @Override public Dimension getPreferredSize() {
            return new Dimension(80, 0);
        }
    };

    private final JPanel renderer = new JPanel(new BorderLayout()) {
        @Override public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(0, d.height);
        }
        @Override public void updateUI() {
            super.updateUI();
            setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        }
    };

    public Component getListCellRendererComponent(JList<? extends OntologyTerm> list, OntologyTerm value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {

        labelL.setText(value.getComments().get("Label"));
        labelR.setText(value.getComments().get("Curie"));

        labelL.setFont(list.getFont());
        labelR.setFont(list.getFont());

        renderer.add(labelL);
        renderer.add(labelR, BorderLayout.EAST);

        // to be updated, not a final version
        renderer.setToolTipText("<html><body><p><b><u>"
                + value.getComments().get("Label")
                + "</u></b></p><p>"
                + value.getComments().get("Curie")
                + "</p><p>"
                + value.getOntologyTermAccession()
                + "</p></body></html>"
        );

        if (index < 0) {
            labelL.setForeground(list.getForeground());
            renderer.setOpaque(false);
        } else {
            labelL.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
            renderer.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            renderer.setOpaque(true);
        }
        return renderer;
    }

}
