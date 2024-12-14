package app.owlcms.nui.preparation;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.vaadin.crudui.crud.CrudOperation;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.FormItem;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep.LabelsPosition;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Binder.Binding;
import com.vaadin.flow.data.binder.ValidationException;

import app.owlcms.components.JXLSDownloader;
import app.owlcms.components.fields.GridField;
import app.owlcms.data.competition.Competition;
import app.owlcms.data.records.RecordConfig;
import app.owlcms.data.records.RecordDefinitionReader;
import app.owlcms.data.records.RecordEvent;
import app.owlcms.data.records.RecordRepository;
import app.owlcms.i18n.Translator;
import app.owlcms.nui.crudui.OwlcmsCrudFormFactory;
import app.owlcms.spreadsheet.JXLSExportRecords;

@SuppressWarnings("serial")
public class RecordConfigEditingFormFactory extends OwlcmsCrudFormFactory<RecordConfig> {

	private class LoadedRecordsField extends GridField<RecordEvent> {

		private Runnable callback;

		public LoadedRecordsField(Runnable callback) {
			super(false, Translator.translate("Records.NoFiles", Translator.translate("Records.UploadButton")));
			this.callback = callback;
		}

		@Override
		protected void createColumns() {
			this.grid.addColumn(RecordEvent::getRecordName);
			this.grid.addColumn(RecordEvent::getAgeGrp).setTextAlign(ColumnTextAlign.CENTER);
			this.grid.addColumn(RecordEvent::getRecordFederation).setTextAlign(ColumnTextAlign.CENTER);
			this.grid.addColumn(RecordEvent::getFileName).setAutoWidth(true);
			this.grid.addComponentColumn(re -> createClearButton(re)).setTextAlign(ColumnTextAlign.CENTER);
		}

		private Button createClearButton(RecordEvent re) {
			Button button = new Button(Translator.translate("Clear"));
			button.addClickListener(e -> {
				RecordRepository.clearByExample(re);
				this.setPresentationValue(RecordConfig.getCurrent().getLoadedFiles());
				this.callback.run();
			});
			return button;
		}

	}

	private GridField<String> orderingField;
	private LoadedRecordsField loadedField;
	private Binding<RecordConfig, List<String>> ofBinding;
	private RecordConfig recordConfig;

	public RecordConfigEditingFormFactory(Class<RecordConfig> domainType) {
		super(domainType);
	}

	@Override
	public RecordConfig add(RecordConfig domainObjectToAdd) {
		throw new UnsupportedOperationException("RecordConfig is a Singleton, cannot add");
	}

	@Override
	public Component buildNewForm(CrudOperation operation, RecordConfig comp, boolean readOnly,
	        ComponentEventListener<ClickEvent<Button>> cancelButtonClickListener,
	        ComponentEventListener<ClickEvent<Button>> updateButtonClickListener,
	        ComponentEventListener<ClickEvent<Button>> deleteButtonClickListener, Button... buttons) {

		this.recordConfig = comp;
		setBinder(buildBinder(operation, comp));

		FormLayout recordsOrderLayout = recordOrderForm();
		FormLayout provisionalLayout = provisionalForm();
		FormLayout officialLayout = officialForm();
		FormLayout exportLayout = exportAllForm();

		TabSheet ts = new TabSheet();
		ts.setWidthFull();
		ts.add(Translator.translate("Records.ConfigurationTab"),
		        new VerticalLayout(
		                recordsOrderLayout,
		                separator(),
		                officialLayout));
		ts.add(Translator.translate("Records.manageNewRecords"),
		        new VerticalLayout(
		                provisionalLayout, separator(), exportLayout));

		VerticalLayout mainLayout = new VerticalLayout(
		        ts);
		mainLayout.setMargin(false);
		mainLayout.setPadding(false);

		this.binder.readBean(comp);
		return mainLayout;
	}

	@Override
	public void delete(RecordConfig domainObjectToDelete) {
		throw new UnsupportedOperationException("RecordConfig is a Singleton, cannot delete");
	}

	@Override
	public Collection<RecordConfig> findAll() {
		return Arrays.asList(RecordConfig.getCurrent());
	}

	@Override
	public RecordConfig update(RecordConfig domainObjectToUpdate) {
		try {
			this.binder.writeBean(domainObjectToUpdate);
		} catch (ValidationException e) {
			throw new RuntimeException("Cannot update RecordConfig {}", e);
		}
		this.recordConfig = RecordConfig.setCurrent(domainObjectToUpdate);
		return this.recordConfig;
	}

	private FormLayout createLayout() {
		FormLayout layout = new FormLayout();
		// layout.setWidth("1024px");
		layout.setResponsiveSteps(new ResponsiveStep("0", 1, LabelsPosition.TOP),
		        new ResponsiveStep("800px", 2, LabelsPosition.TOP));
		return layout;
	}

	private Component createTitle(String string) {
		H4 title = new H4(Translator.translate(string));
		title.getStyle().set("margin-top", "0");
		title.getStyle().set("margin-bottom", "0");
		return title;
	}

	private FormLayout exportAllForm() {
		FormLayout recordsAvailableLayout = createLayout();
		Component title = createTitle("Records.exportAllRecordsTitle");

		recordsAvailableLayout.add(title);
		recordsAvailableLayout.setColspan(title, 2);

		// Div newRecords = DownloadButtonFactory.createDynamicXLSDownloadButton("records",
		// Translator.translate("Records.exportAllRecordsTitle"), new JXLSExportRecords(UI.getCurrent(), true));

		var recordsWriter = new JXLSExportRecords(UI.getCurrent(), true, false);
		JXLSDownloader dd = new JXLSDownloader(
		        () -> {
			        return recordsWriter;
		        },
		        "/templates/records",
		        "exportRecords.xlsx",
		        Translator.translate("Records.exportAllRecordsTitle"),
		        fileName -> fileName.endsWith(".xlsx"));
		Div allRecords = new Div();
		allRecords.add(dd.createImmediateDownloadButton());
		allRecords.setWidthFull();
		
		var recordsWriter1 = new JXLSExportRecords(UI.getCurrent(), true, true);
		JXLSDownloader dd1 = new JXLSDownloader(
		        () -> {
			        return recordsWriter1;
		        },
		        "/templates/records",
		        Competition::getComputedCurrentRecordsTemplateFileName,
		        Competition::setCurrentRecordsTemplateFileName,
		        Translator.translate("Records.exportCurrentRecordsTitle"),
		        Translator.translate("Download"));
		Div allRecords1 = new Div();
		Button downloadButton = dd1.createDownloadButton();
		downloadButton.setWidthFull();
		allRecords1.add(downloadButton);

		recordsAvailableLayout.addFormItem(allRecords, Translator.translate("Records.exportAllRecordsLabel"));
		recordsAvailableLayout.addFormItem(allRecords1, Translator.translate("Records.exportCurrentRecordsLabel"));

		return recordsAvailableLayout;
	}

	private FormLayout officialForm() {
		Button clearNewRecords = new Button(Translator.translate("Records.ClearOfficialRecords"),
		        buttonClickEvent -> {
			        try {
				        RecordRepository.clearOfficialRecords();
				        UI.getCurrent().getPage().reload();
			        } catch (IOException e) {
				        throw new RuntimeException(e);
			        }
		        });
		MemoryBuffer receiver = new MemoryBuffer();

		Button uploadButton = new Button(Translator.translate("Records.UploadButton"));
		uploadButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		Upload uploadRecords = new Upload(receiver);
		uploadRecords.setUploadButton(uploadButton);
		uploadRecords.setDropLabel(new NativeLabel(Translator.translate("Records.UploadDropZone")));
		uploadRecords.addSucceededListener(e -> {
			List<String> errors = RecordDefinitionReader.readInputStream(receiver.getInputStream(),
			        receiver.getFileName());
			if (errors.isEmpty()) {
				UI.getCurrent().getPage().reload();
			} else {
				Pre errorsComponent = new Pre();
				errorsComponent.add(errors.stream().collect(Collectors.joining(System.lineSeparator())));
				Dialog d = new Dialog();
				Button okButton = new Button(Translator.translate("OK"),
				        x -> {
					        d.close();
					        UI.getCurrent().getPage().reload();
				        });
				d.add(errorsComponent);
				d.getFooter().add(okButton);
				d.open();
			}
		});

		FormLayout recordsAvailableLayout = createLayout();
		Component title = createTitle("Records.OfficialSection");

		this.loadedField = new LoadedRecordsField(() -> {
			RecordConfig current = RecordConfig.getCurrent();
			current.addMissing(RecordRepository.findAllRecordNames());
			this.ofBinding.read(current);
		});
		this.loadedField.setWidthFull();
		this.binder.forField(this.loadedField).bind(RecordConfig::getLoadedFiles, RecordConfig::setLoadedFiles);

		recordsAvailableLayout.add(title);
		recordsAvailableLayout.setColspan(title, 2);

		FormItem ur = recordsAvailableLayout.addFormItem(uploadRecords,
		        Translator.translate("Records.UploadOfficialFile"));
		recordsAvailableLayout.setColspan(ur, 1);
		FormItem cni = recordsAvailableLayout.addFormItem(clearNewRecords,
		        Translator.translate("Records.ClearOfficialRecordsExplanation"));
		recordsAvailableLayout.setColspan(cni, 1);

		FormItem lfi = recordsAvailableLayout.addFormItem(this.loadedField,
		        Translator.translate("Records.LoadedOfficialFiles"));
		recordsAvailableLayout.setColspan(lfi, 2);
		return recordsAvailableLayout;
	}

	private FormLayout provisionalForm() {
		Button clearNewRecords = new Button(Translator.translate("Preparation.ClearNewRecords"),
		        buttonClickEvent -> {
			        try {
				        RecordRepository.clearNewRecords();
			        } catch (IOException e) {
				        throw new RuntimeException(e);
			        }
		        });

		FormLayout recordsAvailableLayout = createLayout();
		Component title = createTitle("Records.ProvisionalSection");

		recordsAvailableLayout.add(title);
		recordsAvailableLayout.setColspan(title, 2);

		// Div newRecords = DownloadButtonFactory.createDynamicXLSDownloadButton("records",
		// Translator.translate("Results.NewRecords"), new JXLSExportRecords(UI.getCurrent(), false));

		var recordsWriter = new JXLSExportRecords(UI.getCurrent(), false, false);
		JXLSDownloader dd = new JXLSDownloader(
		        () -> {
			        return recordsWriter;
		        },
		        "/templates/records",
		        "exportRecords.xlsx",
		        Translator.translate("Results.NewRecords"),
		        fileName -> fileName.endsWith(".xlsx"));
		Div newRecords = new Div();
		newRecords.add(dd.createImmediateDownloadButton());
		newRecords.setWidthFull();

		recordsAvailableLayout.addFormItem(newRecords, Translator.translate("Results.NewRecords"));

		clearNewRecords.setWidthFull();
		recordsAvailableLayout.addFormItem(clearNewRecords,
		        Translator.translate("Preparation.ClearNewRecordsExplanation"));

		return recordsAvailableLayout;
	}

	private FormLayout recordOrderForm() {
		Button update = new Button(Translator.translate("Records.UpdateDisplayOptions"));
		update.addClickListener((e) -> this.update(this.recordConfig));
		update.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		VerticalLayout updateContainer = new VerticalLayout(update);
		updateContainer.setAlignSelf(Alignment.END, update);

		FormLayout recordsOrderLayout = createLayout();
		Component title = createTitle("Records.DisplayOptions");
		recordsOrderLayout.add(title);
		recordsOrderLayout.setColspan(title, 1);

		recordsOrderLayout.add(updateContainer);

		this.orderingField = new GridField<>(true,
		        Translator.translate("Records.NoRecords", Translator.translate("Records.UploadButton")));
		this.ofBinding = this.binder.forField(this.orderingField).bind(RecordConfig::getRecordOrder,
		        RecordConfig::setRecordOrder);

		HorizontalLayout ordering = new HorizontalLayout(this.orderingField);
		ordering.setSizeUndefined();
		recordsOrderLayout.addFormItem(ordering, Translator.translate("Records.OrderingField"));

		recordsOrderLayout.add(new Paragraph());

		Checkbox showAllCategoriesField = new Checkbox();
		this.binder.forField(showAllCategoriesField).bind(RecordConfig::getShowAllCategoryRecords,
		        RecordConfig::setShowAllCategoryRecords);
		recordsOrderLayout.addFormItem(showAllCategoriesField, Translator.translate("Records.AllCategories"));

		Checkbox showAllFederationsField = new Checkbox();
		this.binder.forField(showAllFederationsField).bind(RecordConfig::getShowAllFederations,
		        RecordConfig::setShowAllFederations);
		recordsOrderLayout.addFormItem(showAllFederationsField, Translator.translate("Records.AllFederations"));

		return recordsOrderLayout;
	}

	private Hr separator() {
		Hr hr = new Hr();
		hr.getStyle().set("margin-top", "0.5em");
		hr.getStyle().set("margin-bottom", "1.0em");
		hr.getStyle().set("background-color", "var(--lumo-contrast-30pct)");
		hr.getStyle().set("height", "2px");
		return hr;
	}

	private void setBinder(Binder<RecordConfig> buildBinder) {
		this.binder = buildBinder;
	}
}
