package gate.creole.brat.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import gate.Document;
import gate.creole.brat.Annotations;
import gate.creole.brat.BratDocumentFormat;
import gate.creole.metadata.AutoInstance;
import gate.creole.metadata.CreoleResource;
import gate.gui.MainFrame;
import gate.gui.NameBearerHandle;
import gate.gui.ResourceHelper;
import gate.util.ExtensionFileFilter;

@CreoleResource(name = "brat Annotation Importer", tool = true, autoinstances = @AutoInstance, comment = "Add brat annotations to a loaded document")
public class AnnotationImporter extends ResourceHelper {

	private static final long serialVersionUID = 4930907373120018429L;

	private static JComponent dialog = null;

	private static JTextField txtFileName, txtAnnSetName;

	private static FileFilter BRAT_FILE_FILTER = new ExtensionFileFilter("brat Annotation Files (*.ann)", "ann");

	@Override
	protected List<Action> buildActions(NameBearerHandle handle) {

		List<Action> actions = new ArrayList<Action>();

		if (!(handle.getTarget() instanceof Document))
			return actions;

		actions.add(new AbstractAction("Import brat Annotations...") {

			private static final long serialVersionUID = 3512360013967700587L;

			@Override
			public void actionPerformed(ActionEvent e) {

				buildDialog();

				// display the populater dialog and return if it is cancelled
				if (JOptionPane.showConfirmDialog(null, dialog, "Import brat Annotations", JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION)
					return;

				Thread thread = new Thread(Thread.currentThread().getThreadGroup(), "brat Annotation Importer") {

					public void run() {

						try {
							Document doc = (Document) handle.getTarget();
							Annotations annots = new Annotations((new File(txtFileName.getText())).toURI().toURL());
							BratDocumentFormat.merge(doc, txtAnnSetName.getText(), annots);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				};

				// let's leave the GUI nice and responsive
				thread.setPriority(Thread.MIN_PRIORITY);

				// lets get to it and do some actual work!
				thread.start();

			}
		});

		return actions;
	}

	private static void buildDialog() {
		// we'll use the same dialog instance regardless of the corpus we are
		// populating so we'll create a single static instance

		if (dialog != null)
			return;

		dialog = new JPanel();

		dialog.setLayout(new BoxLayout(dialog, BoxLayout.Y_AXIS));

		JButton fileBtn = new JButton(MainFrame.getIcon("OpenFile"));

		// name field
		Box nameBox = Box.createHorizontalBox();
		nameBox.add(Box.createHorizontalStrut(5));
		nameBox.add(new JLabel("brat Annotation File:"));
		nameBox.add(Box.createHorizontalStrut(5));
		txtFileName = new JTextField(30);
		txtFileName.setMaximumSize(new Dimension(Integer.MAX_VALUE, txtFileName.getPreferredSize().height));
		txtFileName.setRequestFocusEnabled(true);
		txtFileName.setVerifyInputWhenFocusTarget(false);
		nameBox.add(txtFileName);
		// nameField.setToolTipText("Enter a name for the resource");

		nameBox.add(Box.createHorizontalStrut(5));
		nameBox.add(fileBtn);
		nameBox.add(Box.createHorizontalGlue());

		Box setBox = Box.createHorizontalBox();
		setBox.add(Box.createHorizontalStrut(5));
		setBox.add(new JLabel("Annotation Set Name:"));
		setBox.add(Box.createHorizontalStrut(5));
		txtAnnSetName = new JTextField(30);
		txtAnnSetName.setMaximumSize(new Dimension(Integer.MAX_VALUE, txtAnnSetName.getPreferredSize().height));
		txtAnnSetName.setVerifyInputWhenFocusTarget(false);
		setBox.add(txtAnnSetName);

		dialog.add(nameBox);
		dialog.add(Box.createVerticalStrut(5));
		dialog.add(setBox);
		dialog.add(Box.createVerticalStrut(5));

		fileBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser filer = MainFrame.getFileChooser();

				filer.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				filer.setDialogTitle("Select a brat Annotation File");
				filer.resetChoosableFileFilters();
				filer.setAcceptAllFileFilterUsed(false);
				filer.addChoosableFileFilter((javax.swing.filechooser.FileFilter) BRAT_FILE_FILTER);
				filer.setFileFilter((javax.swing.filechooser.FileFilter) BRAT_FILE_FILTER);

				if (filer.showOpenDialog(dialog) != JFileChooser.APPROVE_OPTION)
					return;

				txtFileName.setText(filer.getSelectedFile().getAbsolutePath());
			}
		});
	}
}
