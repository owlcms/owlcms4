package app.owlcms.nui.preparation;

import java.util.Set;

import org.vaadin.crudui.crud.CrudOperation;
import org.vaadin.crudui.crud.CrudOperationException;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.SelectionMode;

import app.owlcms.data.group.Group;
import app.owlcms.nui.crudui.OwlcmsCrudFormFactory;
import app.owlcms.nui.crudui.OwlcmsCrudGrid;
import app.owlcms.nui.crudui.OwlcmsGridLayout;

@SuppressWarnings("serial")
final class SessionGrid extends OwlcmsCrudGrid<Group> {
	SessionGrid(Class<Group> domainType, OwlcmsGridLayout crudLayout, OwlcmsCrudFormFactory<Group> owlcmsCrudFormFactory, Grid<Group> grid) {
		super(domainType, crudLayout, owlcmsCrudFormFactory, grid);
	}

	public Set<Group> getSelectedItems() {
		return this.grid.getSelectedItems();
	}

	@Override
	protected void cancelCallback() {
		this.getOwlcmsGridLayout().hideForm();
	}

	@Override
	protected void findAllButtonClicked() {
		refreshGrid();
	}

	@Override
	protected void initLayoutGrid() {
		initToolbar();

		this.grid.setSizeFull();
		this.grid.setSelectionMode(SelectionMode.MULTI);

		// We do not use a selection listener; instead we handle clicks explicitely.
		// grid.addSelectionListener(e -> gridSelectionChanged());
		this.grid.addItemClickListener((e) -> {
			Column<Group> c = e.getColumn();
			if ((c == null) || !this.isClickable()) {
				return;
			}
			long delta = System.currentTimeMillis() - this.clicked;
			if (delta > DOUBLE_CLICK_MS_DELTA) {
				this.grid.select(e.getItem());
				gridSelectionChanged(e.getItem());
			}
			this.clicked = System.currentTimeMillis();
		});
		this.grid.addItemDoubleClickListener((e) -> {
		});

		this.crudLayout.setMainComponent(this.grid);
	}

	@Override
	protected void updateButtons() {
	}

	void updateButtonClicked(Group domainObject) {
		showForm(CrudOperation.UPDATE, domainObject, false, this.savedMessage, event -> {
			try {
				Group updatedObject = this.updateOperation.perform(domainObject);
				this.grid.asSingleSelect().clear();
				refreshGrid();
				this.grid.asSingleSelect().setValue(updatedObject);
				this.grid.deselect(updatedObject);
				showNotification(this.savedMessage);
			} catch (IllegalArgumentException ignore) {
			} catch (CrudOperationException e1) {
				refreshGrid();
				showNotification(e1.getMessage());
				throw e1;
			} catch (Exception e2) {
				refreshGrid();
				throw e2;
			}
		});
	}

	private void gridSelectionChanged(Group item) {
		updateButtons();
		Group domainObject = item;

		if (domainObject != null) {
			updateButtonClicked(item);
		} else {
			this.crudLayout.hideForm();
		}
	}
}