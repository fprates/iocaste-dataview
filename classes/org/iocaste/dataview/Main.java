package org.iocaste.dataview;

import org.iocaste.documents.common.DataElement;
import org.iocaste.documents.common.DataType;
import org.iocaste.documents.common.DocumentModel;
import org.iocaste.documents.common.DocumentModelItem;
import org.iocaste.documents.common.Documents;
import org.iocaste.documents.common.ExtendedObject;
import org.iocaste.shell.common.AbstractPage;
import org.iocaste.shell.common.Button;
import org.iocaste.shell.common.Const;
import org.iocaste.shell.common.Container;
import org.iocaste.shell.common.DataForm;
import org.iocaste.shell.common.DataItem;
import org.iocaste.shell.common.Element;
import org.iocaste.shell.common.Form;
import org.iocaste.shell.common.InputComponent;
import org.iocaste.shell.common.SearchHelp;
import org.iocaste.shell.common.Table;
import org.iocaste.shell.common.TableColumn;
import org.iocaste.shell.common.TableItem;
import org.iocaste.shell.common.TextField;
import org.iocaste.shell.common.ViewData;

public class Main extends AbstractPage {
    private Documents documents;
    
    private final void addTableItem(Table table, ExtendedObject object) {
        Element tfield;
        String name;
        TableItem tableitem = new TableItem(table);
        DocumentModel model = table.getModel();
        
        for (DocumentModelItem modelitem : model.getItens()) {
            name = modelitem.getName();
            
            tfield = new TextField(table, name);
            tfield.setEnabled(!model.isKey(modelitem));
            
            tableitem.add(tfield);
        }
        
        tableitem.setObject(object);
    }
    
    /**
     * 
     * @param vdata
     * @throws Exception
     */
    public final void delete(ViewData vdata) throws Exception {
        Table table = (Table)vdata.getElement("selection_view");
        Documents documents = getDocuments();
        
        for (TableItem item : table.getItens()) {
            if (!item.isSelected())
                continue;
            
            if (documents.delete(item.getObject()) == 0) {
                vdata.message(Const.ERROR, "error.on.delete");
                return;
            }

            documents.commit();
            table.remove(item);
        }
        
        vdata.message(Const.STATUS, "delete.sucessful");
    }
    
    /**
     * 
     * @param vdata
     */
    public final void earlierpage(ViewData vdata) {
        
    }
    
    /**
     * 
     * @param vdata
     * @throws Exception
     */
    public final void edit(ViewData vdata) throws Exception {
        ExtendedObject[] itens;
        String modelname = ((InputComponent)vdata.
                getElement("model.name")).getValue();
        
        try {
            itens = getTableItens(modelname);
        } catch (Exception e) {
            vdata.message(Const.ERROR, e.getMessage());
            return;
        }
        
        vdata.clearParameters();
        vdata.export("mode", "edit");
        vdata.export("view.type", Const.SINGLE);
        vdata.export("model.name", modelname);
        vdata.export("model.regs", itens);
        vdata.setReloadableView(true);
        vdata.redirect(null, "select");
    }
    
    /**
     * 
     * @param vdata
     */
    public final void firstpage(ViewData vdata) {
        
    }
    
    /**
     * 
     * @param vdata
     * @throws Exception
     */
    public final void form(ViewData vdata) throws Exception {
        Container container = new Form(null, "form");
        DataForm form = new DataForm(container, "model.form");
        Documents documents = getDocuments();
        DocumentModel model = documents.getModel(
                (String)vdata.getParameter("model.name"));
        
        form.importModel(model);
        form.setKeyRequired(true);
        new Button(container, "insertitem");
        new Button(container, "insertnext");
        
        vdata.setNavbarActionEnabled("back", true);
        vdata.addContainer(container);
    }
    
    private final Documents getDocuments() {
        if (documents != null)
            return documents;
        
        documents = new Documents(this);
        
        return documents;
    }
    
    /**
     * 
     * @param name
     * @return
     * @throws Exception
     */
    private final ExtendedObject[] getTableItens(String name) throws Exception {
        ExtendedObject[] itens;
        String query;
        Documents documents = getDocuments();
        
        if (!documents.hasModel(name))
            throw new Exception("invalid.model");
            
        query = new StringBuilder("from ").append(name).toString();
        itens = documents.select(query, null);
        
        if (itens == null)
            throw new Exception("table.is.empty");
        
        return itens;
    }
    
    /**
     * 
     * @param vdata
     */
    public final void insert(ViewData vdata) {
        String modelname = (String)vdata.getParameter("model.name");
        
        vdata.clearParameters();
        vdata.export("model.name", modelname);
        vdata.setReloadableView(true);
        vdata.redirect(null, "form");
    }
    
    /**
     * 
     * @param vdata
     * @throws Exception 
     */
    public final void insertcancel(ViewData vdata) throws Exception {
        back(vdata);
    }
    
    /**
     * 
     * @param vdata
     * @throws Exception
     */
    private final void insertcommon(ViewData vdata) throws Exception {
        Table table;
        ViewData selectview;
        DataForm form = (DataForm)vdata.getElement("model.form");
        ExtendedObject object = form.getObject();
        Documents documents = getDocuments();
        
        if (documents.save(object) == 0) {
            vdata.message(Const.ERROR, "duplicated.entry");
            return;
        }
        
        selectview = getView("select");
        table = (Table)selectview.getElement("selection_view");
        addTableItem(table, object);
        updateView(selectview);
    }
    
    /**
     * 
     * @param vdata
     * @throws Exception
     */
    public final void insertitem(ViewData vdata) throws Exception {
        insertcommon(vdata);
        vdata.message(Const.STATUS, "insert.successful");
        back(vdata);
    }
    
    /**
     * 
     * @param vdata
     */
    public final void insertnext(ViewData vdata) throws Exception {
        DataForm form = (DataForm)vdata.getElement("model.form");
        
        insertcommon(vdata);
        form.clearInputs();
        vdata.message(Const.STATUS, "insert.successful");
    }
    
    /**
     * 
     * @param vdata
     */
    public final void lastpage(ViewData vdata) {
        
    }
    
    /**
     * 
     * @param vdata
     */
    public final void laterpage(ViewData vdata) {
        
    }

    /**
     * 
     * @param view
     */
    public void main(ViewData view) {
        Container container = new Form(null, "main");
        DataForm form = new DataForm(container, "model");
        DataItem formitem = new DataItem(form, Const.TEXT_FIELD, "model.name");
        DataElement dataelement = new DataElement();
        SearchHelp sh = new SearchHelp(container, "tablename.search");
        
        dataelement.setLength(20);
        dataelement.setType(DataType.CHAR);
        dataelement.setUpcase(true);
        
        sh.setModelName("MODEL");
        sh.addModelItemName("NAME");
        sh.setExport("NAME");
        
        formitem.setSearchHelp(sh);
        formitem.setDataElement(dataelement);
        formitem.setObligatory(true);
        new Button(container, "edit");
//        form.addAction("show");
        
        view.setFocus("model.name");
        view.setTitle("dataview-selection");
        view.setNavbarActionEnabled("back", true);
        view.addContainer(container);
    }
    
    /**
     * 
     * @param vdata
     * @throws Exception
     */
    public final void save(ViewData vdata) throws Exception {
        String value;
        InputComponent input;
        DocumentModelItem modelitem;
        ExtendedObject object;
        String modelname = (String)vdata.getParameter("model.name");
        Documents documents = getDocuments();
        DocumentModel model = documents.getModel(modelname);
        Table table = ((Table)vdata.getElement("selection_view"));
        
        for (TableItem item : table.getItens()) {
            object = null;
            
            for (Element element: item.getElements()) {
                if (!element.isDataStorable())
                    continue;
                
                input = (InputComponent)element;
                modelitem = input.getModelItem();
                
                value = input.getValue();
                if (value == null && model.isKey(modelitem))
                	break;
                
                if (object == null)
                    object = new ExtendedObject(model);
                
                object.setValue(modelitem, input.getParsedValue());
            }
            
            if (object == null)
                continue;
            
            documents.modify(object);
            documents.commit();
        }
    }
    
    /**
     * 
     * @param view
     * @throws Exception
     */
    public void select(ViewData view) throws Exception {
        boolean key;
        Container container = new Form(null, "dataview.container");
        ExtendedObject[] itens =
        		(ExtendedObject[])view.getParameter("model.regs");
        Documents documents = getDocuments();
        String modelname = (String)view.getParameter("model.name");
        DocumentModel model = documents.getModel(modelname);
        Table table = new Table(container, "selection_view");
        Const viewtype = (Const)view.getParameter("view.type");
        
        table.setMark(true);
        table.importModel(model);
        
        for (TableColumn column: table.getColumns()) {
            if (column.isMark())
                continue;
            
            key = model.isKey(column.getModelItem());
            if (!key && (viewtype == Const.DETAILED))
                column.setVisible(false);
        }
        
        if (itens != null)
            for (ExtendedObject item : itens)
                addTableItem(table, item);
        
        new Button(container, "save");
        new Button(container, "insert");
        new Button(container, "delete");
//        new Button(container, "firstpage").setSubmit(true);
//        new Button(container, "earlierpage").setSubmit(true);
//        new Button(container, "laterpage").setSubmit(true);
//        new Button(container, "lastpage").setSubmit(true);
        
        view.setTitle(modelname);
        view.setNavbarActionEnabled("back", true);
        view.addContainer(container);
    }
    
    /**
     * 
     * @param vdata
     */
    public final void show(ViewData vdata) throws Exception {
        ExtendedObject[] itens;
        String modelname = ((InputComponent)vdata.
                getElement("model.name")).getValue();
        
        try {
            itens = getTableItens(modelname);
        } catch (Exception e) {
            vdata.message(Const.ERROR, e.getMessage());
            return;
        }
        
        vdata.clearParameters();
        vdata.addParameter("mode", "show");
        vdata.addParameter("view.type", Const.SINGLE);
        vdata.addParameter("model.name", modelname);
        vdata.export("model.regs", itens);
        vdata.setReloadableView(true);
        vdata.redirect(null, "select");
    }
}
