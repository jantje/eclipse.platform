package org.eclipse.update.internal.ui.security;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.update.core.IVerificationResult;
import org.eclipse.update.internal.ui.UpdateUI;

/**
 * 
 */
public class JarVerificationPage extends BannerPage {

	private IVerificationResult _VerificationResult = null;
	private String _fileName = null;
	private String _strFeatureName = null;
	private String _strId = null;
	private String _strProviderName = null;
	private TitleAreaDialog _Dialog;
	private boolean okToInstall = false;
	private String componentVerified;

	/*
	 * Constructor for JarVerificationPage.
	 */
	public JarVerificationPage(IVerificationResult verificationResult) {
		super(UpdateUI.getString("JarVerificationDialog.Verification"));
		_fileName = verificationResult.getContentReference().getIdentifier();
		_VerificationResult = verificationResult;
		_strId = verificationResult.getFeature().getVersionedIdentifier().toString();
		_strFeatureName = verificationResult.getFeature().getLabel();
		_strProviderName = verificationResult.getFeature().getProvider();
		componentVerified =	(verificationResult.isFeatureVerification()) ? ".Feature" : ".File";
		okToInstall = false;
	}

	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected Control createContents(Composite compositeParent) {
		WorkbenchHelp.setHelp(compositeParent, "org.eclipse.update.ui.JarVerificationPage");
		// Composite: Client
		//------------------
		Composite compositeClient = new Composite(compositeParent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		compositeClient.setLayout(layout);
		compositeClient.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Text Information
		//------------------		
		createTextArea(compositeClient);

		// Certificate Area
		//------------------
		createCertificateArea(compositeClient);

		// File and Feature Information
		//-----------------------------		
		createInformationArea(compositeClient);

		// Choice Area
		//------------		
		//createChoiceArea(compositeClient);

		return compositeClient;

	}

	/*
	 * Continue install or cancel install
	 */
	private void createChoiceArea(Composite compositeClient) {
		if (_VerificationResult.getVerificationCode()
			!= IVerificationResult.TYPE_ENTRY_CORRUPTED) {

			// Label: Instruction
			//------------------
			Label labelInstruction = new Label(compositeClient, SWT.NULL);
			labelInstruction.setLayoutData(
				new GridData(
					GridData.VERTICAL_ALIGN_BEGINNING
						| GridData.GRAB_VERTICAL
						| GridData.FILL_HORIZONTAL));
			if (_VerificationResult.isFeatureVerification()) {
				labelInstruction.setText(
					UpdateUI.getString("JarVerificationDialog.MayChooseToInstall"));
				//$NON-NLS-1$
			} else {
				labelInstruction.setText(
					UpdateUI.getString("JarVerificationDialog.MayChooseToContinue"));
				//$NON-NLS-1$ 					
			}
			//$NON-NLS-1$
		}
	}

	/*
	 * Creates the Information text
	 */
	private void createTextArea(Composite compositeClient) {

		// Label: Information
		//------------------
		Label labelInformation =
			new Label(compositeClient, SWT.WRAP);
		labelInformation.setLayoutData(
			new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL));

		StringBuffer strb = new StringBuffer();
		switch (_VerificationResult.getVerificationCode()) {

			case IVerificationResult.TYPE_ENTRY_NOT_SIGNED :
				String msg =
					UpdateUI.getString(
						"JarVerificationDialog.AboutToInstall"+
						componentVerified);
				setMessage(msg, WARNING);
				strb.append(
					UpdateUI.getString(
						"JarVerificationDialog.NotDigitallySigned"+
						componentVerified));
				//$NON-NLS-1$
				strb.append("\r\n"); //$NON-NLS-1$
				strb.append(
					UpdateUI.getString(
						"JarVerificationDialog.CannotVerifyProvider"+
						componentVerified));
				//$NON-NLS-1$
/*				strb.append("\r\n"); //$NON-NLS-1$
				if (_VerificationResult.isFeatureVerification()) {
					strb.append(
						UpdateUI.getResourceString("JarVerificationDialog.InstallMayCorrupt"));
					//$NON-NLS-1$
				} else {
					strb.append(
						UpdateUI.getResourceString("JarVerificationDialog.ContinueMayCorrupt"));
					//$NON-NLS-1$ 					
				}
				*/
				labelInformation.setText(strb.toString());
				break;

			case IVerificationResult.TYPE_ENTRY_CORRUPTED :
				msg =
					UpdateUI.getString(
						"JarVerificationDialog.CorruptedContent"+
						componentVerified);
				setMessage(msg, ERROR);
				//$NON-NLS-1$
				strb.append(
					UpdateUI.getString(
						"JarVerificationDialog.ComponentNotInstalled"));
				//$NON-NLS-1$
				labelInformation.setText(strb.toString());
				break;

			case IVerificationResult.TYPE_ENTRY_SIGNED_UNRECOGNIZED :
				msg =
					UpdateUI.getString(
						"JarVerificationDialog.SignedComponent"+
						componentVerified);
				//$NON-NLS-1$
				setMessage(msg, WARNING);
				strb.append(
					UpdateUI.getString(
						"JarVerificationDialog.UnknownCertificate"+
						componentVerified));
				//$NON-NLS-1$
				strb.append("\r\n"); //$NON-NLS-1$
				strb.append(
					UpdateUI.getString(
						"JarVerificationDialog.UnableToVerifyProvider"+
						componentVerified));
				//$NON-NLS-1$
/*				strb.append("\r\n"); //$NON-NLS-1$
				if (_VerificationResult.isFeatureVerification()) {
					strb.append(
						UpdateUI.getResourceString("JarVerificationDialog.InstallMayCorrupt"));
					//$NON-NLS-1$
				} else {
					strb.append(
						UpdateUI.getResourceString("JarVerificationDialog.ContinueMayCorrupt"));
					//$NON-NLS-1$ 					
				}
				*/
				labelInformation.setText(strb.toString());
				break;

			case IVerificationResult.TYPE_ENTRY_SIGNED_RECOGNIZED :
				msg =
					UpdateUI.getString(
						"JarVerificationDialog.SignedComponent"+
						componentVerified);
				//$NON-NLS-1$
				setMessage(msg, WARNING);
				strb.append(
					UpdateUI.getString(
						"JarVerificationDialog.KnownCertificate"+
						componentVerified));
				//$NON-NLS-1$
				strb.append("\r\n"); //$NON-NLS-1$
				strb.append(
					UpdateUI.getString(
						"JarVerificationDialog.ProviderKnown"+
						componentVerified));
				//$NON-NLS-1$
				strb.append("\r\n"); //$NON-NLS-1$

				labelInformation.setText(strb.toString());

//				createCautionArea(compositeClient);
				break;
		}
	}
	
	/*
	 * Caution Label and text
	 */
	private void createCautionArea(Composite compositeClient) {
		// Composite: Caution
		//------------------------------
		Composite compositeCaution = new Composite(compositeClient, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = layout.marginHeight = 0;
		compositeCaution.setLayout(layout);
		compositeCaution.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		// Icon
		//-----
		Label label = new Label(compositeCaution,SWT.LEFT);
		label.setImage(JFaceResources.getImage(TitleAreaDialog.DLG_IMG_MESSAGE_WARNING));
		
		// Text
		//-----
		Label labelInformationCaution =
			new Label(compositeCaution, SWT.WRAP);
		labelInformationCaution.setText(
			UpdateUI.getFormattedMessage(
				"JarVerificationDialog.Caution",
				_strProviderName));
		//$NON-NLS-1$
	}

	/*
	 * Presents File & Feature information
	 */
	private void createInformationArea(Composite compositeClient) {

		// Composite: Information labels
		//------------------------------
		Composite compositeInformation = new Composite(compositeClient, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = layout.marginHeight = 0;
		compositeInformation.setLayout(layout);
		compositeInformation.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Feature name
		//---------------
		Label keyLabel = null;
		CLabel valueLabel = null;
		if (_strFeatureName != null && _strFeatureName.length() > 0) {
			keyLabel = new Label(compositeInformation, SWT.NULL);
			keyLabel.setText(
				UpdateUI.getString("JarVerificationDialog.FeatureName"));
			//$NON-NLS-1$
			valueLabel = new CLabel(compositeInformation, SWT.NULL);
			valueLabel.setFont(JFaceResources.getBannerFont());
			valueLabel.setText(_strFeatureName);
			valueLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		}
		// Feature identifier
		//---------------------
		if (_strId != null && _strId.length() > 0) {
			keyLabel = new Label(compositeInformation, SWT.NULL);
			keyLabel.setText(
				UpdateUI.getString("JarVerificationDialog.FeatureIdentifier"));
			//$NON-NLS-1$
			valueLabel = new CLabel(compositeInformation, SWT.NULL);
			valueLabel.setFont(JFaceResources.getBannerFont());
			valueLabel.setText(_strId);
			valueLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		}
		// Provider name
		//--------------
		if (_strProviderName != null && _strProviderName.length() > 0) {
			keyLabel = new Label(compositeInformation, SWT.NULL);
			keyLabel.setText(
				UpdateUI.getString("JarVerificationDialog.Provider"));
			//$NON-NLS-1$
			valueLabel = new CLabel(compositeInformation, SWT.NULL);
			valueLabel.setFont(JFaceResources.getBannerFont());
			valueLabel.setText(_strProviderName);
			valueLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		}
		// Label: File name
		//-----------------
		keyLabel = new Label(compositeInformation, SWT.NULL);
		keyLabel.setText(
			UpdateUI.getString("JarVerificationDialog.FileName"));
		//$NON-NLS-1$
		valueLabel = new CLabel(compositeInformation, SWT.NULL);
		valueLabel.setFont(JFaceResources.getBannerFont());
		valueLabel.setText(_fileName);
		valueLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	/*
	 * Show certificate information
	 */
	private void createCertificateArea(Composite compositeClient) {

		if (_VerificationResult.getVerificationCode()
			== IVerificationResult.TYPE_ENTRY_SIGNED_UNRECOGNIZED
			|| _VerificationResult.getVerificationCode()
				== IVerificationResult.TYPE_ENTRY_SIGNED_RECOGNIZED) {
			// Group box
			//----------
			Group group = new Group(compositeClient, SWT.SHADOW_ETCHED_IN);
			GridLayout layout = new GridLayout();
			layout.numColumns = 2;
			layout.marginWidth = layout.marginHeight = 0;
			group.setLayout(layout);
			group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			group.setText(UpdateUI.getString("JarVerificationDialog.CertificateInfo"));

			// Signer
			//-------------------
			Label keyLabel = null;
			Text valueText = null;
			//data = new GridData(GridData.FILL_HORIZONTAL);
			//data.horizontalIndent = 0;
			//textInformation.setLayoutData(data);			
			if (_VerificationResult.getSignerInfo() != null) {
				keyLabel = new Label(group, SWT.NULL);
				keyLabel.setText(UpdateUI.getString("JarVerificationDialog.SubjectCA"));
				keyLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
				//$NON-NLS-1$
				valueText = new Text(group, SWT.MULTI|SWT.BORDER|SWT.WRAP|SWT.V_SCROLL);
				valueText.setText(_VerificationResult.getSignerInfo());
				valueText.setEditable(false);
				valueText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			}
			
			// Authenticator
			//---------------------
			if (_VerificationResult.getVerifierInfo() != null) {
				keyLabel = new Label(group, SWT.NULL);
				keyLabel.setText(UpdateUI.getString("JarVerificationDialog.RootCA"));
				keyLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));				
				//$NON-NLS-1$
				valueText = new Text(group, SWT.MULTI|SWT.BORDER|SWT.WRAP|SWT.V_SCROLL);
				valueText.setText(_VerificationResult.getVerifierInfo());
				valueText.setEditable(false);
				valueText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			}
		}
	}

	/*
	 * Sets the Dialog
	 */
	public void setTitleAreaDialog(TitleAreaDialog dialog) {
		_Dialog = dialog;
	};

	/*
	 * 
	 */
	public void setMessage(String newMessage, int newType) {
		super.setMessage(newMessage, newType);
		if (_Dialog != null) {
			_Dialog.setMessage(newMessage, newType);
		}
	}

}