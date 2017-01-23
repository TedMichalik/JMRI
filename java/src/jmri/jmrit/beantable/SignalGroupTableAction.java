package jmri.jmrit.beantable;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SortOrder;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.SignalGroup;
import jmri.SignalHead;
import jmri.SignalMast;
import jmri.swing.RowSorterUtil;
import jmri.util.JmriJFrame;
import jmri.util.SystemNameComparator;
import jmri.util.swing.JmriBeanComboBox;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a SignalGroup Table
 *
 * Based in part on RouteTableAction.java by Bob Jacobsen
 *
 * @author Kevin Dickerson Copyright (C) 2010
 * @author Egbert Broerse 2017
 */
public class SignalGroupTableAction extends AbstractTableAction implements PropertyChangeListener {

    static final ResourceBundle rbx = ResourceBundle.getBundle("jmri.jmrit.beantable.LogixTableBundle");

    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame. Perhaps this should be changed?
     *
     * @param s title of the action
     */
    public SignalGroupTableAction(String s) {
        super(s);
        // disable ourself if there is no primary SignalGroup manager available
        if (jmri.InstanceManager.getNullableDefault(jmri.SignalGroupManager.class) == null) {
            setEnabled(false);
        }

    }

    public SignalGroupTableAction() {
        this(Bundle.getMessage("TitleSignalGroupTable"));
    }

    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("UpdateCondition")) {
            for (int i = _signalHeadsList.size() - 1; i >= 0; i--) {
                SignalGroupSignalHead signalHead = _signalHeadsList.get(i);
                SignalHead sigBean = signalHead.getBean();
                if (curSignalGroup.isHeadIncluded(sigBean)) {
                    signalHead.setIncluded(true);
                    signalHead.setOnState(curSignalGroup.getHeadOnState(sigBean));
                    signalHead.setOffState(curSignalGroup.getHeadOffState(sigBean));
                } else {
                    signalHead.setIncluded(false);
                }
            }
        }
    }

    /**
     * Create the JTable DataModel, along with the changes for the specific case
     * of SignalGroups
     */
    protected void createModel() {
        m = new BeanTableDataModel() {
            static public final int COMMENTCOL = 2;
            static public final int DELETECOL = 3;
            static public final int ENABLECOL = 4;
            static public final int EDITCOL = 5; // default name: SETCOL

            @Override
            public int getColumnCount() {
                return 6;
            }

            @Override
            public String getColumnName(int col) {
                if (col == EDITCOL) {
                    return "";    // no heading on "Edit" column
                }
                if (col == ENABLECOL) {
                    return Bundle.getMessage("ColumnHeadEnabled");
                }
                if (col == COMMENTCOL) {
                    return Bundle.getMessage("ColumnComment");
                }
                if (col == DELETECOL) {
                    return "";
                } else {
                    return super.getColumnName(col);
                }
            }

            @Override
            public Class<?> getColumnClass(int col) {
                if (col == EDITCOL) {
                    return JButton.class;
                }
                if (col == ENABLECOL) {
                    return Boolean.class;
                }
                if (col == DELETECOL) {
                    return JButton.class;
                }
                if (col == COMMENTCOL) {
                    return String.class;
                } else {
                    return super.getColumnClass(col);
                }
            }

            @Override
            public int getPreferredWidth(int col) {
                if (col == EDITCOL) {
                    return new JTextField(Bundle.getMessage("ButtonEdit")).getPreferredSize().width;
                }
                if (col == ENABLECOL) {
                    return new JTextField(6).getPreferredSize().width;
                }
                if (col == COMMENTCOL) {
                    return new JTextField(30).getPreferredSize().width;
                }
                if (col == DELETECOL) {
                    return new JTextField(22).getPreferredSize().width;
                } else {
                    return super.getPreferredWidth(col);
                }
            }

            @Override
            public boolean isCellEditable(int row, int col) {
                if (col == COMMENTCOL) {
                    return true;
                }
                if (col == EDITCOL) {
                    return true;
                }
                if (col == ENABLECOL) {
                    return true;
                }
                if (col == DELETECOL) {
                    return true;
                } else {
                    return super.isCellEditable(row, col);
                }
            }

            @Override
            public Object getValueAt(int row, int col) {
                NamedBean b;
                if (col == EDITCOL) {
                    return Bundle.getMessage("ButtonEdit");
                } else if (col == ENABLECOL) {
                    return Boolean.valueOf(((SignalGroup) getBySystemName((String) getValueAt(row, SYSNAMECOL))).getEnabled());
                    //return true;
                } else if (col == COMMENTCOL) {
                    b = getBySystemName(sysNameList.get(row));
                    return (b != null) ? b.getComment() : null;
                } else if (col == DELETECOL) //
                {
                    return Bundle.getMessage("ButtonDelete");
                } else {
                    return super.getValueAt(row, col);
                }
            }

            @Override
            public void setValueAt(Object value, int row, int col) {
                if (col == EDITCOL) {
                    // set up to Edit. Use separate Runnable so window is created on top
                    class WindowMaker implements Runnable {

                        int row;

                        WindowMaker(int r) {
                            row = r;
                        }

                        public void run() {
                            addPressed(null); // set up add/edit panel addFrame (starts as Add pane)
                            _systemName.setText((String) getValueAt(row, SYSNAMECOL));
                            editPressed(null); // adjust addFrame for Edit
                        }
                    }
                    WindowMaker t = new WindowMaker(row);
                    javax.swing.SwingUtilities.invokeLater(t);
                } else if (col == ENABLECOL) {
                    // alternate
                    SignalGroup r = (SignalGroup) getBySystemName((String) getValueAt(row, SYSNAMECOL));
                    boolean v = r.getEnabled();
                    r.setEnabled(!v);
                } else if (col == COMMENTCOL) {
                    getBySystemName(sysNameList.get(row)).setComment(
                            (String) value);
                    fireTableRowsUpdated(row, row);
                } else if (col == DELETECOL) {
                    // button fired, delete Bean
                    deleteBean(row, col);
                } else {
                    super.setValueAt(value, row, col);
                }
            }

            @Override
            public void configureTable(JTable table) {
                table.setDefaultRenderer(Boolean.class, new EnablingCheckboxRenderer());
                table.setDefaultRenderer(JComboBox.class, new jmri.jmrit.symbolicprog.ValueRenderer());
                table.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());
                super.configureTable(table);
            }

            protected void configDeleteColumn(JTable table) {
                // have the delete column hold a button
                SignalGroupTableAction.this.setColumnToHoldButton(table, DELETECOL,
                        new JButton(Bundle.getMessage("ButtonDelete")));
            }

            /**
             * Delete the bean after all the checking has been done.
             * <P>
             * (Deactivate the Signal Group), then use the superclass to delete it.
             */
            void doDelete(NamedBean bean) {
                //((SignalGroup)bean).deActivateSignalGroup();
                super.doDelete(bean);
            }

            // want to update when enabled parameter changes
            protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
                if (e.getPropertyName().equals("Enabled")) {
                    return true;
                } //if (e.getPropertyName().equals("Locked")) return true;
                else {
                    return super.matchPropertyName(e);
                }
            }

            public Manager getManager() {
                return jmri.InstanceManager.getDefault(jmri.SignalGroupManager.class);
            }

            public NamedBean getBySystemName(String name) {
                return jmri.InstanceManager.getDefault(jmri.SignalGroupManager.class).getBySystemName(name);
            }

            public NamedBean getByUserName(String name) {
                return jmri.InstanceManager.getDefault(jmri.SignalGroupManager.class).getByUserName(name);
            }

            public int getDisplayDeleteMsg() {
                return 0x00;/*return InstanceManager.getDefault(jmri.UserPreferencesManager.class).getWarnDeleteSignalGroup();*/ }

            public void setDisplayDeleteMsg(int boo) { /*InstanceManager.getDefault(jmri.UserPreferencesManager.class).setWarnDeleteSignalGroup(boo); */

            }

            protected String getMasterClassName() {
                return getClassName();
            }

            @Override
            public void clickOn(NamedBean t) { // mute action
                //((SignalGroup)t).setSignalGroup();
            }

            @Override
            public String getValue(String s) { // not directly used but should be present to implement abstract class
                return "Set";
            }

/*            public JButton configureButton() {
                return new JButton(" Set ");
            }*/

            protected String getBeanType() {
                return "Signal Group";
            }
        };
    }

    @Override
    protected void setTitle() {
        f.setTitle(Bundle.getMessage("TitleSignalGroupTable"));
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.SignalGroupTable";
    }

    /**
     * Read Appearance for a Signal Group Signal Head from the state comboBox
     * called from SignalGroupSubTableAction
     * @param box to read from
     */
    int signalStateFromBox(JComboBox<String> box) {
        String mode = (String) box.getSelectedItem();
        int result = jmri.util.StringUtil.getStateFromName(mode, signalStatesValues, signalStates);

        if (result < 0) {
            log.warn("unexpected mode string in signalState Aspect: " + mode);
            throw new IllegalArgumentException();
        }
        return result;
    }

    /**
     * Sets Appearance in a Signal Group Signal Head state comboBox
     * called from SignalGroupSubTableAction
     * @param mode Value to be set
     * @param box in which to enter mode
     */
    void setSignalStateBox(int mode, JComboBox<String> box) {
        String result = jmri.util.StringUtil.getNameFromState(mode, signalStatesValues, signalStates);
        box.setSelectedItem(result);
    }

    JTextField _systemName = new JTextField(10);
    JTextField _userName = new JTextField(22);

    JmriJFrame addFrame = null;

    SignalGroupSignalHeadModel _SignalGroupHeadModel;
    JScrollPane _SignalGroupHeadScrollPane;

    SignalMastAspectModel _AspectModel;
    JScrollPane _SignalAppearanceScrollPane;

    JmriBeanComboBox mainSignal;

    ButtonGroup selGroup = null;
    JRadioButton allButton = null;
    JRadioButton includedButton = null;

    JLabel nameLabel = new JLabel(Bundle.getMessage("LabelSystemName"));
    JLabel userLabel = new JLabel(Bundle.getMessage("LabelUserName"));
    JLabel fixedSystemName = new JLabel("xxxxxxxxxxx");

    JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
    JButton deleteButton = new JButton(Bundle.getMessage("ButtonDelete") + " " + Bundle.getMessage("BeanNameSignalGroup"));
    JButton updateButton = new JButton(Bundle.getMessage("ButtonApply"));

    JPanel p2xs = null;   // Container for...
    JPanel p2xsi = null;  // SignalHead list table
    JPanel p3xsi = null;

    SignalGroup curSignalGroup = null;
    boolean SignalGroupDirty = false;  // true to fire reminder to save work
    boolean inEditMode = false; // to warn and prevent opening more than 1 editing session

    /**
     * Respond to click on Add... button below Signal Group Table
     * Create JPanel with options
     * @param e Event from origin; null when called from Edit button in Signal Group Table row
     */
    protected void addPressed(ActionEvent e) {
        if (inEditMode) {
            log.debug("Can not open another editing session for Signal Groups.");
            // add user warning that a 2nd session not allowed (cf. Logix)
            // Already editing a Signal Group, ask for completion of that edit first
            String workingTitle = _systemName.getText();
            if (workingTitle == "" || workingTitle == null) {
                workingTitle = Bundle.getMessage("NONE");
                _systemName.setText(workingTitle);
            }
            javax.swing.JOptionPane.showMessageDialog(addFrame,
                    Bundle.getMessage("SigGroupEditBusyWarning", workingTitle),
                    Bundle.getMessage("ErrorTitle"),
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }

        inEditMode = true;
        _mastAspectsList = null;

        jmri.SignalHeadManager shm = InstanceManager.getDefault(jmri.SignalHeadManager.class);
        List<String> systemNameList = shm.getSystemNameList();
        _signalHeadsList = new ArrayList<SignalGroupSignalHead>(systemNameList.size());
        // create list of all available Single Output Signal Heads to choose from
        Iterator<String> iter = systemNameList.iterator();
        // int i = 1; // for debug of iter next
        while (iter.hasNext()) {
            String systemName = iter.next();
            SignalHead sh = shm.getBySystemName(systemName);
            // log.debug("Iteration {} of : Looking for Signal Head {}", i, systemNameList.size(), systemName);
            // debug using i & sysnamelist.size
            if (sh != null) {
                if (sh.getClass().getName().contains("SingleTurnoutSignalHead")) {
                    String userName = sh.getUserName();
                    // add every single output signal head item to the list
                    _signalHeadsList.add(new SignalGroupSignalHead(systemName, userName));
                } else {
                    log.debug("Signal Head " + systemName + " is not a Single Output Controlled Signal Head");
                }
            } else { // this is not an error and the value of systemName mentioned is actually from the last head that was indeed loaded
                log.error("Failed to get signal head {} (SGTA)", systemName);
            }
        }

        // Set up Add/Edit SignalGroup window
        if (addFrame == null) { // if it's not yet present, create addFrame
            mainSignal = new JmriBeanComboBox(jmri.InstanceManager.getDefault(jmri.SignalMastManager.class), null, JmriBeanComboBox.DISPLAYNAME);
            mainSignal.setFirstItemBlank(true); // causes NPE when user selects that 1st line, so do not respond to result null
            addFrame = new JmriJFrame(Bundle.getMessage("AddSignalGroup"), false, true);
            addFrame.addHelpMenu("package.jmri.jmrit.beantable.SignalGroupAddEdit", true);
            addFrame.setLocation(100, 30);
            addFrame.getContentPane().setLayout(new BoxLayout(addFrame.getContentPane(), BoxLayout.Y_AXIS));
            Container contentPane = addFrame.getContentPane();
            // add system name
            JPanel ps = new JPanel();
            ps.setLayout(new FlowLayout());
            ps.add(nameLabel);
            ps.add(_systemName);
            _systemName.setToolTipText(Bundle.getMessage("SignalGroupSysNameTooltip"));
            ps.add(fixedSystemName);
            fixedSystemName.setVisible(false);
            contentPane.add(ps);
            // add user name
            JPanel p = new JPanel();
            p.setLayout(new FlowLayout());
            p.add(userLabel);
            p.add(_userName);
            _userName.setToolTipText(Bundle.getMessage("SignalGroupUserNameTooltip"));
            contentPane.add(p);
            
            // add Signal Masts/Heads Display Choice
            JPanel py = new JPanel();
            py.add(new JLabel(Bundle.getMessage("Show")));
            selGroup = new ButtonGroup();
            allButton = new JRadioButton(Bundle.getMessage("All"), true);
            selGroup.add(allButton);
            py.add(allButton);
            allButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // Setup for display of all Signal Masts & SingleTO Heads, if needed
                    if (!showAll) {
                        showAll = true;
                        _SignalGroupHeadModel.fireTableDataChanged();
                        _AspectModel.fireTableDataChanged();
                    }
                }
            });
            includedButton = new JRadioButton(Bundle.getMessage("Included"), false);
            selGroup.add(includedButton);
            py.add(includedButton);
            includedButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // Setup for display of included Turnouts only, if needed
                    if (showAll) {
                        showAll = false;
                        initializeIncludedList();
                        _SignalGroupHeadModel.fireTableDataChanged();
                        _AspectModel.fireTableDataChanged();

                    }
                }
            });
            py.add(new JLabel("  " + Bundle.getMessage("_and_", Bundle.getMessage("LabelAspects"),
                    Bundle.getMessage("SignalHeads"))));
            contentPane.add(py);

            // add main signal mast table
            JPanel p3 = new JPanel();
            p3.setLayout(new BoxLayout(p3, BoxLayout.Y_AXIS));
            JPanel p31 = new JPanel();
            p31.add(new JLabel(Bundle.getMessage("EnterMastAttached", Bundle.getMessage("BeanNameSignalMast"))));
            p3.add(p31);
            JPanel p32 = new JPanel();
            p32.add(new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameSignalMast"))));
            p32.add(mainSignal); // comboBox to pick a main Signal Mast
            p3.add(p32);

            p3xsi = new JPanel();
            JPanel p3xsiSpace = new JPanel();
            p3xsiSpace.setLayout(new BoxLayout(p3xsiSpace, BoxLayout.Y_AXIS));
            p3xsiSpace.add(new JLabel(" "));
            p3xsi.add(p3xsiSpace);

            JPanel p31si = new JPanel();
            p31si.setLayout(new BoxLayout(p31si, BoxLayout.Y_AXIS));
            p31si.add(new JLabel(Bundle.getMessage("SelectAppearanceTrigger")));

            p3xsi.add(p31si);
            _AspectModel = new SignalMastAspectModel();
            JTable SignalMastAspectTable = new JTable(_AspectModel);
            TableRowSorter<SignalMastAspectModel> smaSorter = new TableRowSorter<>(_AspectModel);
            smaSorter.setComparator(SignalMastAspectModel.ASPECT_COLUMN, new SystemNameComparator());
            RowSorterUtil.setSortOrder(smaSorter, SignalMastAspectModel.ASPECT_COLUMN, SortOrder.ASCENDING);
            SignalMastAspectTable.setRowSorter(smaSorter);
            SignalMastAspectTable.setRowSelectionAllowed(false);
            SignalMastAspectTable.setPreferredScrollableViewportSize(new java.awt.Dimension(200, 80));
            TableColumnModel SignalMastAspectColumnModel = SignalMastAspectTable.getColumnModel();
            TableColumn includeColumnA = SignalMastAspectColumnModel.
                    getColumn(SignalGroupTableAction.SignalMastAspectModel.INCLUDE_COLUMN);
            includeColumnA.setResizable(false);
            includeColumnA.setMinWidth(30);
            includeColumnA.setMaxWidth(60);
            @SuppressWarnings("static-access")
            TableColumn sNameColumnA = SignalMastAspectColumnModel.
                    getColumn(_AspectModel.ASPECT_COLUMN);
            sNameColumnA.setResizable(true);
            sNameColumnA.setMinWidth(75);
            sNameColumnA.setMaxWidth(140);

            _SignalAppearanceScrollPane = new JScrollPane(SignalMastAspectTable);
            p3xsi.add(_SignalAppearanceScrollPane, BorderLayout.CENTER);
            p3.add(p3xsi);
            p3xsi.setVisible(true);

            mainSignal.addActionListener( // respond to comboBox selection
                new ActionListener() {
                    //public void focusGained(FocusEvent e) {
                    //}
                    public void actionPerformed(ActionEvent event) {
                        if (mainSignal.getSelectedBean() == null) { // ie. empty first row was selected or set
                            log.debug("Empty line in mainSignal comboBox");
                        } else {
                            if (curSignalGroup == null ||
                                    mainSignal.getSelectedBean() != curSignalGroup.getSignalMast()) {
                                log.debug("comboBox closed, choice: {}", mainSignal.getSelectedItem());
                                setValidSignalMastAspects(); // refresh table with signal mast aspects
                            } else {
                                log.debug("Mast {} picked in mainSignal comboBox", mainSignal.getSelectedItem());
                            }
                        }
                    }
                }
            );
            
            // complete this panel
            Border p3Border = BorderFactory.createEtchedBorder();
            p3.setBorder(p3Border);
            contentPane.add(p3);

            p2xsi = new JPanel();
            JPanel p2xsiSpace = new JPanel();
            p2xsiSpace.setLayout(new BoxLayout(p2xsiSpace, BoxLayout.Y_AXIS));
            p2xsiSpace.add(new JLabel("XXX"));
            p2xsi.add(p2xsiSpace);

            JPanel p21si = new JPanel();
            p21si.setLayout(new BoxLayout(p21si, BoxLayout.Y_AXIS));
            p21si.add(new JLabel(Bundle.getMessage("SelectInGroup", Bundle.getMessage("SignalHeads"))));
            p2xsi.add(p21si);
            _SignalGroupHeadModel = new SignalGroupSignalHeadModel();
            JTable SignalGroupHeadTable = new JTable(_SignalGroupHeadModel);
            TableRowSorter<SignalGroupSignalHeadModel> sgsSorter = new TableRowSorter<>(_SignalGroupHeadModel);
            sgsSorter.setComparator(SignalGroupSignalHeadModel.SNAME_COLUMN, new SystemNameComparator());
            RowSorterUtil.setSortOrder(sgsSorter, SignalGroupSignalHeadModel.SNAME_COLUMN, SortOrder.ASCENDING);
            SignalGroupHeadTable.setRowSorter(sgsSorter);
            SignalGroupHeadTable.setRowSelectionAllowed(false);
            SignalGroupHeadTable.setPreferredScrollableViewportSize(new java.awt.Dimension(480, 160));
            TableColumnModel SignalGroupSignalColumnModel = SignalGroupHeadTable.getColumnModel();

            TableColumn includeColumnSi = SignalGroupSignalColumnModel.
                    getColumn(SignalGroupSignalHeadModel.INCLUDE_COLUMN);
            includeColumnSi.setResizable(false);
            includeColumnSi.setMinWidth(30);
            includeColumnSi.setMaxWidth(60);

            TableColumn sNameColumnSi = SignalGroupSignalColumnModel.
                    getColumn(SignalGroupSignalHeadModel.SNAME_COLUMN);
            sNameColumnSi.setResizable(true);
            sNameColumnSi.setMinWidth(75);
            sNameColumnSi.setMaxWidth(95);

            TableColumn uNameColumnSi = SignalGroupSignalColumnModel.
                    getColumn(SignalGroupSignalHeadModel.UNAME_COLUMN);
            uNameColumnSi.setResizable(true);
            uNameColumnSi.setMinWidth(100);
            uNameColumnSi.setMaxWidth(260);

            TableColumn stateOnColumnSi = SignalGroupSignalColumnModel.
                    getColumn(SignalGroupSignalHeadModel.STATE_ON_COLUMN); // a 6 column table
            stateOnColumnSi.setResizable(false);
            stateOnColumnSi.setMinWidth(Bundle.getMessage("SignalHeadStateFlashingYellow").length()); // was 50
            stateOnColumnSi.setMaxWidth(100);

            TableColumn stateOffColumnSi = SignalGroupSignalColumnModel.
                    getColumn(SignalGroupSignalHeadModel.STATE_OFF_COLUMN);
            stateOffColumnSi.setResizable(false);
            stateOffColumnSi.setMinWidth(50);
            stateOffColumnSi.setMaxWidth(100);

            TableColumn editColumnSi = SignalGroupSignalColumnModel.
                    getColumn(SignalGroupSignalHeadModel.EDIT_COLUMN);
            editColumnSi.setResizable(false);
            editColumnSi.setMinWidth(Bundle.getMessage("ButtonEdit").length()); // was 50
            editColumnSi.setMaxWidth(100);
            JButton editButton = new JButton(Bundle.getMessage("ButtonEdit"));
            setColumnToHoldButton(SignalGroupHeadTable, SignalGroupSignalHeadModel.EDIT_COLUMN, editButton);

            _SignalGroupHeadScrollPane = new JScrollPane(SignalGroupHeadTable);
            p2xsi.add(_SignalGroupHeadScrollPane, BorderLayout.CENTER);
            p2xsi.setToolTipText(Bundle.getMessage("SignalGroupHeadTableTooltip")); // add tooltip to explain which head types are shown
            contentPane.add(p2xsi);
            p2xsi.setVisible(true);

            // add notes panel, may be empty (a dot on the screen)
            JPanel pa = new JPanel();
            pa.setLayout(new BoxLayout(pa, BoxLayout.Y_AXIS));

            Border pBorder = BorderFactory.createEtchedBorder();
            pa.setBorder(pBorder);
            contentPane.add(pa);

            // buttons at bottom of panel
            JPanel pb = new JPanel();
            pb.setLayout(new FlowLayout(FlowLayout.TRAILING));

            pb.add(cancelButton);
            cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    cancelPressed(e);
                }
            });
            cancelButton.setVisible(false); // do not show in 4.7.1 while working on NPE fix
            pb.add(deleteButton);
            deleteButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    deletePressed(e);
                }
            });
            deleteButton.setToolTipText(Bundle.getMessage("DeleteSignalGroupInSystem"));
            // Update SignalGroup button in Add/Edit SignalGroup pane
            pb.add(updateButton);
            updateButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    updatePressed(e, false, true);
                }
            });
            updateButton.setToolTipText(Bundle.getMessage("TooltipUpdate"));
            updateButton.setVisible(true);
            contentPane.add(pb);
            // pack and release space
            addFrame.pack();
            p2xsiSpace.setVisible(false);
        } // set listener for window closing
        else {
            mainSignal.setSelectedBean(null);
            addFrame.setTitle(Bundle.getMessage("AddSignalGroup")); // reset title for new group
        }
        addFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                // remind to save, if SignalGroup was created or edited
                if (SignalGroupDirty) {
                    InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                            showInfoMessage(Bundle.getMessage("ReminderTitle"),
                                    Bundle.getMessage("ReminderSaveString", Bundle.getMessage("SignalGroup")),
                                    "beantable.SignalGroupTableAction",
                                    "remindSignalGroup"); // NOI18N
                    SignalGroupDirty = false;
                }
                // hide addFrame
                if (addFrame != null) {addFrame.setVisible(false);} // hide first, could be gone by the time of the close event,
                // so prevent NPE
                inEditMode = false; // release editing soon, as long as NPEs occor in the following methods
                finishUpdate();
                _SignalGroupHeadModel.dispose();
                _AspectModel.dispose();
            }
        });
        // display the pane
        addFrame.setVisible(true);
    }

    void setColumnToHoldButton(JTable table, int column, JButton sample) {
        // install a button renderer & editor
        ButtonRenderer buttonRenderer = new ButtonRenderer();
        table.setDefaultRenderer(JButton.class, buttonRenderer);
        TableCellEditor buttonEditor = new ButtonEditor(new JButton());
        table.setDefaultEditor(JButton.class, buttonEditor);
        // ensure the table rows, columns have enough room for buttons
        table.setRowHeight(sample.getPreferredSize().height);
        table.getColumnModel().getColumn(column)
                .setPreferredWidth((sample.getPreferredSize().width) + 4);
    }

    /**
     * Initialize list of included signal head appearances for when "Included" is selected
     */
    void initializeIncludedList() {
        _includedMastAspectsList = new ArrayList<SignalMastAspect>();
        for (int i = 0; i < _mastAspectsList.size(); i++) {
            if (_mastAspectsList.get(i).isIncluded()) {
                _includedMastAspectsList.add(_mastAspectsList.get(i));
            }

        }
        _includedSignalHeadsList = new ArrayList<SignalGroupSignalHead>();
        for (int i = 0; i < _signalHeadsList.size(); i++) {
            if (_signalHeadsList.get(i).isIncluded()) {
                _includedSignalHeadsList.add(_signalHeadsList.get(i));
            }
        }
    }

    /**
     * Check name for a new SignalGroup object using the _systemName field on the addFrame pane
     * @return If name is allowed
     */
    boolean checkNewNamesOK() {
        // Get system name and user name from Add Signal Group pane
        String sName = _systemName.getText().toUpperCase().trim();
        // seems field _systemName is not properly filled in when editing an existing mast
        // so prevent it from being called (in line 900)
        String uName = _userName.getText(); // may be empty
        if (sName.length() == 0) {
            javax.swing.JOptionPane.showMessageDialog(null, "System Name field can not be left blank.",
                    Bundle.getMessage("ErrorTitle"), javax.swing.JOptionPane.WARNING_MESSAGE);
            log.debug("Empty system name field for Signal Group [{}]", sName);
            return false;
        }
        SignalGroup g = null;
        // check if a SignalGroup with the same user name exists
        if (!uName.equals("")) {
            g = jmri.InstanceManager.getDefault(jmri.SignalGroupManager.class).getByUserName(uName);
            if (g != null) {
                // SignalGroup with this user name already exists
                javax.swing.JOptionPane.showMessageDialog(null, Bundle.getMessage("SignalGroupDuplicateUserNameWarning", uName),
                        Bundle.getMessage("ErrorTitle"), javax.swing.JOptionPane.WARNING_MESSAGE);
                return false;
            } else {
                return true;
            }
        }
        // check if a SignalGroup with this system name already exists
        g = jmri.InstanceManager.getDefault(jmri.SignalGroupManager.class).getBySystemName(sName);
        if (g != null) {
            // SignalGroup already exists
            javax.swing.JOptionPane.showMessageDialog(null, Bundle.getMessage("SignalGroupDuplicateSystemNameWarning", sName),
                    Bundle.getMessage("ErrorTitle"), javax.swing.JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    boolean checkValidSignalMast() {
        SignalMast mMast = (SignalMast) mainSignal.getSelectedBean();
        if (mMast == null) {
            log.warn("Signal Mast not selected. mainSignal = {}", mainSignal.getSelectedItem());
            javax.swing.JOptionPane.showMessageDialog(null, Bundle.getMessage("NoMastSelectedWarning"),
                    Bundle.getMessage("ErrorTitle"), javax.swing.JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    /**
     * Check name and return a new or existing SignalGroup object
     * SignalGroup with the name as entered in the _systemName field on the addFrame pane
     */
    SignalGroup checkNamesOK() {
        // Get system name and user name
        String sName = _systemName.getText().toUpperCase();
        String uName = _userName.getText();
        if (sName.length() == 0) {
            javax.swing.JOptionPane.showMessageDialog(null, Bundle.getMessage("RouteAddStatusEnter"),
                    Bundle.getMessage("ErrorTitle"), javax.swing.JOptionPane.WARNING_MESSAGE);
            // Reuse a key with general wording
            return null;
        }
        try {
            SignalGroup g = jmri.InstanceManager.getDefault(jmri.SignalGroupManager.class).provideSignalGroup(sName, uName);
            return g;
        } catch (IllegalArgumentException ex) {
            // should never get here
            log.error("checkNamesOK; Unknown failure to create SignalGroup with System Name: " + sName);
            throw ex;
        }   
    }

    int setSignalInformation(SignalGroup g) {
        for (int i = 0; i < g.getNumHeadItems(); i++) {
            SignalHead sig = g.getHeadItemBeanByIndex(i);
            boolean valid = false;
            for (int x = 0; x < _includedSignalHeadsList.size(); x++) {
                SignalGroupSignalHead sh = _includedSignalHeadsList.get(x);
                if (sig == sh.getBean()) {
                    valid = true;
                    break;
                }
            }
            if (!valid) {
                g.deleteSignalHead(sig);
            }
        }
        for (int i = 0; i < _includedSignalHeadsList.size(); i++) {
            SignalGroupSignalHead s = _includedSignalHeadsList.get(i);
            SignalHead sig = s.getBean();
            if (!g.isHeadIncluded(sig)) {
                g.addSignalHead(sig);
                g.setHeadOnState(sig, s.getOnStateInt());
                g.setHeadOffState(sig, s.getOffStateInt());
            }
        }
        return _includedSignalHeadsList.size();
    }

    /**
     * Store included Aspects for the selected main Signal Mast in the SignalGroup
     * @param g Signal Group object
     */
    void setMastAspectInformation(SignalGroup g) {
        g.clearSignalMastAspect();
        for (int x = 0; x < _includedMastAspectsList.size(); x++) {
            g.addSignalMastAspect(_includedMastAspectsList.get(x).getAspect());
        }
    }

    /**
     * Look up the list of valid Aspects for the selected main Signal Mast in the comboBox
     * and store them in a table on the addFrame using _AspectModel
     */
    void setValidSignalMastAspects() {
        jmri.SignalMast sm = (SignalMast) mainSignal.getSelectedBean();
        if (sm == null) {
            log.debug("Null picked in mainSignal comboBox. Probably line 1 or no masts in system");
            return;
        }
        log.debug("Mast {} picked in mainSignal comboBox", mainSignal.getSelectedItem());
        java.util.Vector<String> aspects = sm.getValidAspects();

        _mastAspectsList = new ArrayList<SignalMastAspect>(aspects.size());
        for (int i = 0; i < aspects.size(); i++) {
            _mastAspectsList.add(new SignalMastAspect(aspects.get(i)));
        }
        _AspectModel.fireTableDataChanged();
    }

    /**
     * When user clicks Cancel during editing a Signal Group, closes the Add/Edit pane
     * and reset default entries
     * @param e Event from origin
     */
    void cancelPressed(ActionEvent e) {
        log.debug("Cancelled; addFrame exists = {}", (addFrame != null));
        if (addFrame != null) {addFrame.setVisible(false);} // hide first, may cause NPE uncheked
        inEditMode = false; // release editing soon, as NPEs may occur in the following methods
        finishUpdate();
        _SignalGroupHeadModel.dispose();
        _AspectModel.dispose();
        log.debug("cancelPressed in SGTA line 871");
    }

    /**
     * Responds to the Edit button in the Signal Group Table
     * after creating the Add/Edit pane with AddPressed supplying _SystemName
     * Hides the editable _systemName field and displays it as a label
     * @param e Event from origin, null if invoked by clicking the Edit button in a Signal Group Table row
     */
    void editPressed(ActionEvent e) {
        // identify the SignalGroup with this name if it already exists
        String sName = _systemName.getText().toUpperCase(); // is already filled in from the Signal Group table by addPressed()
        SignalGroup g = jmri.InstanceManager.getDefault(jmri.SignalGroupManager.class).getBySystemName(sName);
        if (g == null) {
            // SignalGroup does not exist, so cannot be edited
            return;
        }
        g.addPropertyChangeListener(this);

        // SignalGroup was found, make its system name not changeable
        curSignalGroup = g;
        log.debug("curSignalGroup was set");

        jmri.SignalMast sm = jmri.InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast(g.getSignalMastName());
        if (sm != null ) {
            java.util.Vector<String> aspects = sm.getValidAspects();
            _mastAspectsList = new ArrayList<SignalMastAspect>(aspects.size());

            for (int i = 0; i < aspects.size(); i++) {
                _mastAspectsList.add(new SignalMastAspect(aspects.get(i)));
            }
        } else {
            log.error("Failed to get signal mast {}", g.getSignalMastName()); // false indicates Can't find mast (but quoted name stands for a head) TODO
        }

        fixedSystemName.setText(sName);
        fixedSystemName.setVisible(true);
        _systemName.setVisible(false);
        mainSignal.setSelectedBean(g.getSignalMast());
        _userName.setText(g.getUserName());

        int setRow = 0;

        for (int i = _signalHeadsList.size() - 1; i >= 0; i--) {
            SignalGroupSignalHead sgsh = _signalHeadsList.get(i);
            SignalHead sigBean = sgsh.getBean();
            if (g.isHeadIncluded(sigBean)) {
                sgsh.setIncluded(true);
                sgsh.setOnState(g.getHeadOnState(sigBean));
                sgsh.setOffState(g.getHeadOffState(sigBean));
                setRow = i;
            } else {
                sgsh.setIncluded(false);
            }
        }
        _SignalGroupHeadScrollPane.getVerticalScrollBar().setValue(setRow * ROW_HEIGHT);
        _SignalGroupHeadModel.fireTableDataChanged();

        for (int i = 0; i < _mastAspectsList.size(); i++) {
            SignalMastAspect _aspect = _mastAspectsList.get(i);
            String asp = _aspect.getAspect();
            if (g.isSignalMastAspectIncluded(asp)) {
                _aspect.setIncluded(true);
                setRow = i;
            } else {
                _aspect.setIncluded(false);
            }
        }
        _SignalAppearanceScrollPane.getVerticalScrollBar().setValue(setRow * ROW_HEIGHT);

        _AspectModel.fireTableDataChanged();
        initializeIncludedList();

        SignalGroupDirty = true;  // to fire reminder to save work
        updateButton.setVisible(true);
        fixedSystemName.setVisible(true);
        _systemName.setVisible(false);
        addFrame.setTitle(Bundle.getMessage("EditSignalGroup"));
        inEditMode = true; // to block opening another edit session
    }

    /**
     * Responds to the Delete button in the Add/Edit pane
     */
    void deletePressed(ActionEvent e) {
        InstanceManager.getDefault(jmri.SignalGroupManager.class).deleteSignalGroup(curSignalGroup);
        curSignalGroup = null;
        log.debug("DeletePressed; curSignalGroup set to null");
        finishUpdate();
    }

    /**
     * Responds to the Update button - store new properties in SignalGroup
     * @param e Event from origin, null if invoked by clicking the Edit button in a Signal Group Table row
     * @param newSignalGroup False when called as Update, True after editing Signal Head details
     * @param close True if the pane is closing, False if it stays open
     */
    void updatePressed(ActionEvent e, boolean newSignalGroup, boolean close) {
        log.debug("Update found SignalGroup system name: {}/{}", _systemName.getText(), fixedSystemName.getText());
        if (_systemName.getText() == "") {
            _systemName.setText(fixedSystemName.getText());
            // NPE in checkNewNamesOK() because the _systemName field seems to be empty
        }
        String uName = _userName.getText();
        if (curSignalGroup == null) {
            log.error("Catched NPE during Update. curSignalGroup = null");
            // We might want to check if the User Name has been changed. But there's
            // nothing to compare with so this is propably a newly created signalGroup.
            // TODO cannot be compared since curSignalGroup is null, causes NPE
            // method sends repeated false warning when editing an existing SignalGroup
            // for which a system name is visibly filled in
            if (!checkNewNamesOK()) {
                return;
            }
        }
        if (!checkValidSignalMast()) {
            return;
        }
        SignalGroup g = checkNamesOK(); // if this fails, we are stuck
        if (g == null) { // error logging/dialog handled in checkNamesOK()
            return;
        }
        curSignalGroup = g;
        // user name is unique, change it
        g.setUserName(uName);
        initializeIncludedList();
        setSignalInformation(g);
        setMastAspectInformation(g);

        g.setSignalMast((SignalMast) mainSignal.getSelectedBean(), mainSignal.getSelectedDisplayName());
        SignalGroupDirty = true;  // to fire reminder to save work
        if (close) {
            finishUpdate();
            inEditMode = false;
        }
    }

    /**
     * Clean up the Edit Signal Group pane
     */
    void finishUpdate() {
        if (curSignalGroup != null) {
            curSignalGroup.removePropertyChangeListener(this);
        }
        _systemName.setVisible(true);
        fixedSystemName.setVisible(false);
        _systemName.setText("");
        _userName.setText("");
        mainSignal.setSelectedBean(null); // empty the "main mast" comboBox
        if (_signalHeadsList == null) {
            // prevent NPE when clicking Cancel/close pane with no work done, after first showing (no mast selected)
            log.debug("FinishUpdate; _signalHeadsList empty; no heads present");
        } else {
            for (int i = _signalHeadsList.size() - 1; i >= 0; i--) {
                _signalHeadsList.get(i).setIncluded(false);
            }
        }
        if (_mastAspectsList == null) {
            // prevent NPE when clicking Cancel/close pane with no work done, after first showing (no mast selected)
            log.debug("FinishUpdate; _mastAspectsList empty; no mast was selected");
        } else {
            for (int i = _mastAspectsList.size() - 1; i >= 0; i--) {
                _mastAspectsList.get(i).setIncluded(false);
            }
        }
        showAll = true;
        curSignalGroup = null;
        log.debug("FinishUpdate; curSignalGroup set to null. Hiding addFrame next");
        if (addFrame != null) {addFrame.setVisible(false);}
    }

    public class SignalMastAspectModel extends AbstractTableModel implements PropertyChangeListener {

        @Override
        public Class<?> getColumnClass(int c) {
            if (c == INCLUDE_COLUMN) {
                return Boolean.class;
            } else {
                return String.class;
            }
        }

        @Override
        public String getColumnName(int col) {
            if (col == INCLUDE_COLUMN) {
                return Bundle.getMessage("Include");
            }
            if (col == ASPECT_COLUMN) {
                return Bundle.getMessage("LabelAspectType");
                // list contains Signal Mast Aspects (might be called "Appearances" by some but in code keep to JMRI bean names and Help)
            }
            return "";
        }

        public void dispose() {
            InstanceManager.getDefault(jmri.SignalMastManager.class).removePropertyChangeListener(this);
        }

        @Override
        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if (e.getPropertyName().equals("length")) {
                // a new NamedBean is available in the manager
                fireTableDataChanged();
            }
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            return ((c == INCLUDE_COLUMN));
        }

        public static final int ASPECT_COLUMN = 0;
        public static final int INCLUDE_COLUMN = 1;

        public void setSetToState(String x) {
        }

        @Override
        public int getRowCount() {
            if (_mastAspectsList == null) {
                return 0;
            }
            if (showAll) {
                return _mastAspectsList.size();
            } else {
                return _includedMastAspectsList.size();
            }
        }

        @Override
        public Object getValueAt(int r, int c) {
            ArrayList<SignalMastAspect> aspectList = null;
            if (showAll) {
                aspectList = _mastAspectsList;
            } else {
                aspectList = _includedMastAspectsList;
            }
            // some error checking
            if (_mastAspectsList == null || r >= aspectList.size()) {
                // prevent NPE when clicking Add... in table to add new group (with 1 group existing using a different mast type)
                log.debug("SGTA getValueAt #1123: row value {} is greater than aspectList size {}", r, aspectList.size());
                return null;
            }
            switch (c) {
                case INCLUDE_COLUMN:
                    return Boolean.valueOf(aspectList.get(r).isIncluded());
                case ASPECT_COLUMN:
                    return aspectList.get(r).getAspect();
                default:
                    return null;
            }
        }

        @Override
        public void setValueAt(Object type, int r, int c) {
            log.debug("SigGroupEditSet A; row = {}");
            ArrayList<SignalMastAspect> aspectList = null;
            if (showAll) {
                aspectList = _mastAspectsList;
            } else {
                aspectList = _includedMastAspectsList;
            }
            if (_mastAspectsList == null || r >= aspectList.size()) {
                // prevent NPE when closing window after NPE in getValueAdd() happened
                log.debug("row value {} is greater than aspectList size {}", r, aspectList);
                return;
            }
            log.debug("SigGroupEditSet B; row = {}; aspectList.size() = {}.", r, aspectList.size());
            switch (c) {
                case INCLUDE_COLUMN:
                    aspectList.get(r).setIncluded(((Boolean) type).booleanValue()); // NPE
                    break;
                case ASPECT_COLUMN:
                    aspectList.get(r).setAspect((String) type);
                    break;
                default:
                    break;
            }
        }

    }

    /**
     * Base table model for managing generic Signal Group outputs
     */
    public abstract class SignalGroupOutputModel extends AbstractTableModel implements PropertyChangeListener {

        @Override
        public Class<?> getColumnClass(int c) {
            if (c == INCLUDE_COLUMN) {
                return Boolean.class;
            } else {
                return String.class;
            }
        }

        @Override
        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if (e.getPropertyName().equals("length")) {
                // a new NamedBean is available in the manager
                fireTableDataChanged();
            } else if (e.getPropertyName().equals("UpdateCondition")) {
                fireTableDataChanged();
            }
        }

        @Override
        public String getColumnName(int c) {
            return COLUMN_NAMES[c];
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            return ((c == INCLUDE_COLUMN) || (c == STATE_COLUMN));
        }

        public static final int SNAME_COLUMN = 0;
        public static final int UNAME_COLUMN = 1;
        public static final int INCLUDE_COLUMN = 2;
        public static final int STATE_COLUMN = 3;

    }

    /**
     * Table Model to manage Signal Head outputs in a Signal Group
     */
    class SignalGroupSignalHeadModel extends SignalGroupOutputModel {

        SignalGroupSignalHeadModel() {
            InstanceManager.getDefault(jmri.SignalHeadManager.class).addPropertyChangeListener(this);
        }

        public boolean isCellEditable(int r, int c) {
            return ((c == INCLUDE_COLUMN) || (c == STATE_ON_COLUMN) || (c == STATE_OFF_COLUMN) || (c == EDIT_COLUMN));
        }

        public int getColumnCount() {
            return 6;
        }

        public static final int STATE_ON_COLUMN = 3;
        public static final int STATE_OFF_COLUMN = 4;
        public static final int EDIT_COLUMN = 5;

        @Override
        public Class<?> getColumnClass(int c) {
            if (c == INCLUDE_COLUMN) {
                return Boolean.class;
            } else if (c == EDIT_COLUMN) {
                return JButton.class;
            } else {
                return String.class;
            }
        }

        @Override
        public String getColumnName(int c) {
            return COLUMN_SIG_NAMES[c];
        }

        public void setSetToState(String x) {
        }

        /**
         * The number of rows in the Signal Head table
         * @return The number of rows
         */
        @Override
        public int getRowCount() {
            if (showAll) {
                return _signalHeadsList.size();
            } else {
                return _includedSignalHeadsList.size();
            }
        }

        /**
         * Fill in info cells of the Signal Head table on the Add/Edit Group Edit pane
         * @param r Index of the cell row
         * @param c Index of the cell column
         */
        @Override
        public Object getValueAt(int r, int c) {
            ArrayList<SignalGroupSignalHead> headsList = null;
            if (showAll) {
                headsList = _signalHeadsList;
            } else {
                headsList = _includedSignalHeadsList;
            }
            // some error checking
            if (r >= headsList.size()) {
                log.debug("Row num {} is greater than headsList size {}", r, headsList.size());
                return null;
            }
            switch (c) {
                case INCLUDE_COLUMN:
                    return Boolean.valueOf(headsList.get(r).isIncluded());
                case SNAME_COLUMN:
                    return headsList.get(r).getSysName();
                case UNAME_COLUMN:
                    return headsList.get(r).getUserName();
                case STATE_ON_COLUMN:
                    return headsList.get(r).getOnState();
                case STATE_OFF_COLUMN:
                    return headsList.get(r).getOffState();
                case EDIT_COLUMN:
                    return (Bundle.getMessage("ButtonEdit"));
                default:
                    return null;
            }
        }

        /**
         * Fetch User Name (System Name if User Name is empty) for a row in the Signal Head table
         * @return bean object
         */
        public String getDisplayName(int r) {
            if (((String) getValueAt(r, UNAME_COLUMN) != null) && (!((String) getValueAt(r, UNAME_COLUMN)).equals(""))) {
                return (String) getValueAt(r, UNAME_COLUMN);
            } else {
                return (String) getValueAt(r, SNAME_COLUMN);
            }
        }

        /**
         * Fetch existing bean object for a row in the Signal Head table
         * @return bean object
         */
        public SignalHead getBean(int r) {
            return jmri.InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead((String) getValueAt(r, SNAME_COLUMN));
        }

        /**
         * Store info from the cells of the Signal Head table of the Add/Edit Group Edit pane
         * @param type The contents from the table
         * @param r Index of the cell row of the entry
         * @param c Index of the cell column of the entry
         */
        @Override
        public void setValueAt(Object type, int r, int c) {
            ArrayList<SignalGroupSignalHead> headsList = null;
            if (showAll) {
                headsList = _signalHeadsList;
            } else {
                headsList = _includedSignalHeadsList;
            }
            switch (c) {
                case INCLUDE_COLUMN:
                    headsList.get(r).setIncluded(((Boolean) type).booleanValue());
                    break;
                case STATE_ON_COLUMN:
                    headsList.get(r).setSetOnState((String) type);
                    break;
                case STATE_OFF_COLUMN:
                    headsList.get(r).setSetOffState((String) type);
                    break;
                case EDIT_COLUMN:
                    headsList.get(r).setIncluded(((Boolean) true).booleanValue());
                    class WindowMaker implements Runnable {

                        final int row;

                        WindowMaker(int r) {
                            row = r;
                        }

                        public void run() {
                            signalHeadEditPressed(row);
                        }
                    }
                    WindowMaker t = new WindowMaker(r);
                    javax.swing.SwingUtilities.invokeLater(t);
                    break;
                default:
                    break;
            }
        }

        /**
         * Remove listener from Signal Head in group
         * Called on Delete
         */
        public void dispose() {
            InstanceManager.getDefault(jmri.SignalHeadManager.class).removePropertyChangeListener(this);
        }
    }

    JmriJFrame signalHeadEditFrame = null;

    /**
     * Opens an editor to set the details of a Signal Head as part of a Signal Group
     * when user clicks the Edit button in the Signal Head table in the lower half of the Edit Signal Group pane.
     * (renamed from signalEditPressed in 4.7.1 to explain what's in here)
     * @param row Index of line clicked in the displayed Signal Head table
     */
    void signalHeadEditPressed(int row) {
        if (curSignalGroup == null) {
            log.debug("From signalEditPressed");
            if (!checkNewNamesOK()) {
                log.debug("signalHeadEditPressed: checkNewNamesOK = false");
                return;
            }
            if (!checkValidSignalMast()) {
                return;
            }
            updatePressed(null, true, false);
            // Read the new entries provided in the Add pane before opening the Edit Signal Head subpane
        }
        if (!curSignalGroup.isHeadIncluded(_SignalGroupHeadModel.getBean(row))) {
            curSignalGroup.addSignalHead(_SignalGroupHeadModel.getBean(row));
        }
        _SignalGroupHeadModel.fireTableDataChanged();
        log.debug("signalHeadEditPressed: opening sbaTableAction for edit");
        SignalGroupSubTableAction editSignalHead = new SignalGroupSubTableAction();
        // calls separate class file SignalGroupSubTableAction to edit details for head
        editSignalHead.editHead(curSignalGroup, _SignalGroupHeadModel.getDisplayName(row));
    }

    private boolean showAll = true; // false indicates: show only included Signal Masts & SingleTO Heads

    private static int ROW_HEIGHT;

    private static String[] COLUMN_NAMES = { // used in class SignalGroupOutputModel (Turnouts and Sensors)
            Bundle.getMessage("ColumnSystemName"),
            Bundle.getMessage("ColumnUserName"),
            Bundle.getMessage("Include"),
            Bundle.getMessage("ColumnLabelSetState")
    };
    private static String[] COLUMN_SIG_NAMES = { // used in class SignalGroupSignalModel
            Bundle.getMessage("ColumnSystemName"),
            Bundle.getMessage("ColumnUserName"),
            Bundle.getMessage("Include"),
            Bundle.getMessage("OnAppearance"),
            Bundle.getMessage("OffAppearance"),
            "" // No label above last (Edit) column
    };

    private static String[] signalStates = new String[]{Bundle.getMessage("SignalHeadStateDark"), Bundle.getMessage("SignalHeadStateRed"), Bundle.getMessage("SignalHeadStateYellow"), Bundle.getMessage("SignalHeadStateGreen"), Bundle.getMessage("SignalHeadStateLunar")};
    private static int[] signalStatesValues = new int[]{SignalHead.DARK, SignalHead.RED, SignalHead.YELLOW, SignalHead.GREEN, SignalHead.LUNAR};

    private ArrayList<SignalGroupSignalHead> _signalHeadsList;        // array of all single output signal heads
    private ArrayList<SignalGroupSignalHead> _includedSignalHeadsList; // subset of heads included in sh table

    private ArrayList<SignalMastAspect> _mastAspectsList;        // array of all valid aspects for the main signal mast
    private ArrayList<SignalMastAspect> _includedMastAspectsList; // subset of aspects included in asp table

    /**
     * Class to store definition of a Signal Head as part of a Signal Group.
     * Includes properties for what to display
     * (renamed from SignalGroupSignal in 4.7.1 to explain what's in here)
     */
    private static class SignalGroupSignalHead {

        SignalHead _signalHead = null;
        boolean _included;

        /**
         * Create an object to hold name and configuration of a Signal Head as part of a Signal Group
         * Filters only existing Single Turnout Signal Heads from the loaded configuration
         * Used while editing Signal Groups
         * Contains whether it is included in a group, the On state and Off state
         * @param sysName System Name of the grouphead
         * @param userName Optional User Name
         */
        SignalGroupSignalHead(String sysName, String userName) {
            _included = false;
            SignalHead anySigHead = InstanceManager.getDefault(jmri.SignalHeadManager.class).getBySystemName(sysName);
            if (anySigHead != null) {
                if (anySigHead.getClass().getName().contains("SingleTurnoutSignalHead")) {
                    jmri.implementation.SingleTurnoutSignalHead oneSigHead = (jmri.implementation.SingleTurnoutSignalHead) InstanceManager.getDefault(jmri.SignalHeadManager.class).getBySystemName(sysName);
                    if (oneSigHead != null) {
                        _onState = oneSigHead.getOnAppearance();
                        _offState = oneSigHead.getOffAppearance();
                        _signalHead = oneSigHead;
                    } else {
                        log.error("Failed to get oneSigHead head {}", sysName);
                    }
                }
            } else {
                log.error("Failed to get signal head {}",  sysName);
            }

        }

        SignalHead getBean() {
            return _signalHead;
        }

        String getSysName() {
            return _signalHead.getSystemName();
        }

        String getUserName() {
            return _signalHead.getUserName();
        }

        boolean isIncluded() {
            return _included;
        }

        void setIncluded(boolean include) {
            _included = include;
        }

        /**
         * Retrieve On setting for Signal Head in Signal Group
         * Should match entries in setOnState()
         * @return localized string as the name for the Signal Head Appearance when this head is On
         */
        String getOnState() {
            switch (_onState) {
                case SignalHead.DARK:
                    return Bundle.getMessage("SignalHeadStateDark");
                case SignalHead.RED:
                    return Bundle.getMessage("SignalHeadStateRed");
                case SignalHead.YELLOW:
                    return Bundle.getMessage("SignalHeadStateYellow");
                case SignalHead.GREEN:
                    return Bundle.getMessage("SignalHeadStateGreen");
                case SignalHead.LUNAR:
                    return Bundle.getMessage("SignalHeadStateLunar");
                case SignalHead.FLASHRED:
                    return Bundle.getMessage("SignalHeadStateFlashingRed");
                case SignalHead.FLASHYELLOW:
                    return Bundle.getMessage("SignalHeadStateFlashingYellow");
                case SignalHead.FLASHGREEN:
                    return Bundle.getMessage("SignalHeadStateFlashingGreen");
                case SignalHead.FLASHLUNAR:
                    return Bundle.getMessage("SignalHeadStateFlashingLunar");
            }
            return "";
        }

        /**
         * Retrieve Off setting for Signal Head in Signal Group
         * Should match entries in setOffState()
         * @return localized string as the name for the Signal Head Appearance when this head is Off
         */
        String getOffState() {
            switch (_offState) {
                case SignalHead.DARK:
                    return Bundle.getMessage("SignalHeadStateDark");
                case SignalHead.RED:
                    return Bundle.getMessage("SignalHeadStateRed");
                case SignalHead.YELLOW:
                    return Bundle.getMessage("SignalHeadStateYellow");
                case SignalHead.GREEN:
                    return Bundle.getMessage("SignalHeadStateGreen");
                case SignalHead.LUNAR:
                    return Bundle.getMessage("SignalHeadStateLunar");
                case SignalHead.FLASHRED:
                    return Bundle.getMessage("SignalHeadStateFlashingRed");
                case SignalHead.FLASHYELLOW:
                    return Bundle.getMessage("SignalHeadStateFlashingYellow");
                case SignalHead.FLASHGREEN:
                    return Bundle.getMessage("SignalHeadStateFlashingGreen");
                case SignalHead.FLASHLUNAR:
                    return Bundle.getMessage("SignalHeadStateFlashingLunar");
            }
            return "";
        }

        int getOnStateInt() {
            return _onState;
        }

        int getOffStateInt() {
            return _offState;
        }

        /**
         * Store On setting for Signal Head in Signal Group
         * Should match entries in getOnState()
         * @param localized name for the Signal Head Appearance when this head is On
         */
        void setSetOnState(String state) {
            if (state.equals(Bundle.getMessage("SignalHeadStateDark"))) {
                _onState = SignalHead.DARK;
            } else if (state.equals(Bundle.getMessage("SignalHeadStateRed"))) {
                _onState = SignalHead.RED;
            } else if (state.equals(Bundle.getMessage("SignalHeadStateYellow"))) {
                _onState = SignalHead.YELLOW;
            } else if (state.equals(Bundle.getMessage("SignalHeadStateGreen"))) {
                _onState = SignalHead.GREEN;
            } else if (state.equals(Bundle.getMessage("SignalHeadStateLunar"))) {
                _onState = SignalHead.LUNAR;
            } else if (state.equals(Bundle.getMessage("SignalHeadStateFlashingRed"))) {
                _onState = SignalHead.FLASHRED;
            } else if (state.equals(Bundle.getMessage("SignalHeadStateFlashingYellow"))) {
                _onState = SignalHead.FLASHYELLOW;
            } else if (state.equals(Bundle.getMessage("SignalHeadStateFlashingGreen"))) {
                _onState = SignalHead.FLASHGREEN;
            } else if (state.equals(Bundle.getMessage("SignalHeadStateFlashingLunar"))) {
                _onState = SignalHead.FLASHLUNAR;
            }
        }

        /**
         * Store Off setting for Signal Head in Signal Group
         * Should match entries in getOffState()
         * @param localized name for the Signal Head Appearance when this head is Off
         */
        void setSetOffState(String state) {
            if (state.equals(Bundle.getMessage("SignalHeadStateDark"))) {
                _offState = SignalHead.DARK;
            } else if (state.equals(Bundle.getMessage("SignalHeadStateRed"))) {
                _offState = SignalHead.RED;
            } else if (state.equals(Bundle.getMessage("SignalHeadStateYellow"))) {
                _offState = SignalHead.YELLOW;
            } else if (state.equals(Bundle.getMessage("SignalHeadStateGreen"))) {
                _offState = SignalHead.GREEN;
            } else if (state.equals(Bundle.getMessage("SignalHeadStateLunar"))) {
                _offState = SignalHead.LUNAR;
            } else if (state.equals(Bundle.getMessage("SignalHeadStateFlashingRed"))) {
                _offState = SignalHead.FLASHRED;
            } else if (state.equals(Bundle.getMessage("SignalHeadStateFlashingYellow"))) {
                _offState = SignalHead.FLASHYELLOW;
            } else if (state.equals(Bundle.getMessage("SignalHeadStateFlashingGreen"))) {
                _offState = SignalHead.FLASHGREEN;
            } else if (state.equals(Bundle.getMessage("SignalHeadStateFlashingLunar"))) {
                _offState = SignalHead.FLASHLUNAR;
            }
        }

        int _onState = 0x00;
        int _offState = 0x00;

        public void setOnState(int state) {
            _onState = state;
        }

        public void setOffState(int state) {
            _offState = state;
        }
    }

    /**
     * Class to store definition of main Signal Mast in a Signal Group
     * (renamed from SignalMastAppearance in 4.7.1 to explain what's in here)
     */
    private static class SignalMastAspect {

        SignalMastAspect(String aspect) {
            _aspect = aspect;
        }

        boolean _include;
        String _aspect;

        void setIncluded(boolean include) {
            _include = include;
        }

        boolean isIncluded() {
            return _include;
        }

        void setAspect(String asp) {
            _aspect = asp;
        }

        String getAspect() {
            return _aspect;
        }

    }

    protected String getClassName() {
        return SignalGroupTableAction.class.getName();
    }

    public String getClassDescription() {
        return Bundle.getMessage("TitleSignalGroupTable");
    }

    private final static Logger log = LoggerFactory.getLogger(SignalGroupTableAction.class.getName());
}
