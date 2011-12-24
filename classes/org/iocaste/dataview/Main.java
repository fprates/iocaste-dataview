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
import org.iocaste.shell.common.ControlData;
import org.iocaste.shell.common.DataForm;
import org.iocaste.shell.common.DataItem;
import org.iocaste.shell.common.Element;
import org.iocaste.shell.common.Form;
import org.iocaste.shell.common.InputComponent;
import org.iocaste.shell.common.SearchHelp;
import org.iocaste.shell.common.Table;
import org.iocaste.shell.common.TableItem;
import org.iocaste.shell.common.ViewData;

public class Main extends AbstractPage {
    private Documents documents;
    
    private final void addTableItem(Table table, Element[] elements,
            ExtendedObject object) {
        Element tfield;
        String name;
        TableItem tableitem = new TableItem(table);
        DocumentModel model = table.getModel();
        
        for (DocumentModelItem modelitem : model.getItens()) {
            name = modelitem.getName();
            tableitem.add(Const.TEXT_FIELD, name, null);
            tfield = table.getElement(tableitem.getComplexName(name));
            tfield.setEnabled(!model.isKey(modelitem));
        }
        
        tableitem.setObject(object);
    }
    
    /**
     * 
     * @param cdata
     * @param vdata
     * @throws Exception
     */
    public final void delete(ControlData cdata, ViewData vdata) 
            throws Exception {
        Table table = (Table)vdata.getElement("selection_view");
        Documents documents = getDocuments();
        
        for (TableItem item : table.getSelected()) {
            if (documents.delete(item.getObject()) == 0) {
                cdata.message(Const.ERROR, "error.on.delete");
                return;
            }

            documents.commit();
            table.remove(item);
        }
        
        cdata.message(Const.STATUS, "delete.sucessful");
    }
    
    /**
     * 
     * @param cdata
     * @param vdata
     */
    public final void earlierpage(ControlData cdata, ViewData vdata) {
        
    }
    
    /**
     * 
     * @param controldata
     * @param view
     * @throws Exception
     */
    public final void edit(ControlData cdata, ViewData vdata) 
            throws Exception {
        ExtendedObject[] itens;
        String query, modelname = ((InputComponent)vdata.
                getElement("model.name")).getValue();
        Documents documents = getDocuments();
        
        if (!documents.hasModel(modelname)) {
            cdata.message(Const.ERROR, "invalid.model");
            return;
        }
            
        query = new StringBuilder("from ").append(modelname).toString();
        itens = documents.select(query, null);
        
        if (itens == null)
            cdata.message(Const.WARNING, "table.is.empty");
        
        cdata.clearParameters();
        cdata.addParameter("mode", "edit");
        cdata.addParameter("view.type", Const.SINGLE);
        cdata.addParameter("model.name", modelname);
        cdata.addParameter("model.regs", itens);
        cdata.setReloadableView(true);
        cdata.redirect(null, "select");
    }
    
    /**
     * 
     * @param cdata
     * @param vdata
     */
    public final void firstpage(ControlData cdata, ViewData vdata) {
        
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
        form.addAction("insertitem");
        form.addAction("insertnext");
        
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
     * @param cdata
     * @param vdata
     */
    public final void insert(ControlData cdata, ViewData vdata) {
        cdata.clearParameters();
        cdata.addParameter("model.name", vdata.getParameter("model.name"));
        cdata.setReloadableView(true);
        cdata.redirect(null, "form");
    }
    
    /**
     * 
     * @param cdata
     * @param vdata
     * @throws Exception 
     */
    public final void insertcancel(ControlData cdata, ViewData vdata)
            throws Exception {
        back(cdata, vdata);
    }
    
    private final void insertcommon(ControlData cdata, ViewData vdata)
            throws Exception {
        Table table;
        ViewData selectview;
        DataForm form = (DataForm)vdata.getElement("model.form");
        ExtendedObject object = form.getObject();
        Documents documents = getDocuments();
        
        if (documents.save(object) == 0) {
            cdata.message(Const.ERROR, "duplicated.entry");
            return;
        }
        
        selectview = getView("select");
        table = (Table)selectview.getElement("selection_view");
        addTableItem(table, table.getElements(), object);
        updateView(selectview);
    }
    
    /**
     * 
     * @param cdata
     * @param vdata
     * @throws Exception
     */
    public final void insertitem(ControlData cdata, ViewData vdata) 
            throws Exception {
        insertcommon(cdata, vdata);
        cdata.message(Const.STATUS, "insert.successful");
        back(cdata, vdata);
    }
    
    /**
     * 
     * @param cdata
     * @param vdata
     */
    public final void insertnext(ControlData cdata, ViewData vdata) 
            throws Exception {
        DataForm form = (DataForm)vdata.getElement("model.form");
        
        insertcommon(cdata, vdata);
        form.clearInputs();
        cdata.message(Const.STATUS, "insert.successful");
    }
    
    /**
     * 
     * @param cdata
     * @param vdata
     */
    public final void lastpage(ControlData cdata, ViewData vdata) {
        
    }
    
    /**
     * 
     * @param cdata
     * @param vdata
     */
    public final void laterpage(ControlData cdata, ViewData vdata) {
        
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
        form.addAction("edit");
//        form.addAction("show");
        
        view.setFocus("model.name");
        view.setTitle("dataview.selection");
        view.setNavbarActionEnabled("back", true);
        view.addContainer(container);
    }
    
    /**
     * 
     * @param cdata
     * @param vdata
     * @throws Exception
     */
    public final void save(ControlData cdata, ViewData vdata) throws Exception {
        TableItem tableitem;
        String value;
        InputComponent input;
        DocumentModelItem modelitem;
        Element cell;
        ExtendedObject object;
        String modelname = (String)vdata.getParameter("model.name");
        Documents documents = getDocuments();
        DocumentModel model = documents.getModel(modelname);
        Table table = ((Table)vdata.getElement("selection_view"));
        
        for (Element element : table.getElements()) {
            if (element.getType() != Const.TABLE_ITEM)
                continue;
            
            tableitem = (TableItem)element;
            object = null;
            
            for (String name : tableitem.getElementNames()) {
                cell = table.getElement(name);
                
                if (!cell.isDataStorable())
                    continue;
                
                input = (InputComponent)cell;
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
        Element[] elements;
        boolean key;
        Container container = new Form(null, "dataview.container");
        int i = 0;
        ExtendedObject[] itens =
        		(ExtendedObject[])view.getParameter("model.regs");
        Documents documents = getDocuments();
        DocumentModel model = documents.getModel(
                (String)view.getParameter("model.name"));
        Table table = new Table(container, 0, "selection_view");
        Const viewtype = (Const)view.getParameter("view.type");
        
        table.setMark(true);
        table.importModel(model);
        
        elements = table.getElements();
        
        for (Element element_ : elements) {
            i++;
            if (element_.getType() != Const.DATA_ITEM)
                continue;
            
            key = model.isKey(((DataItem)element_).getModelItem());
            if (!key && (viewtype == Const.DETAILED))
                table.setVisibleColumn(i, false);
        }
        
        if (itens != null)
            for (int k = table.getFirstItem(); k < itens.length; k++)
                addTableItem(table, elements, itens[k]);
        
        new Button(container, "save").setSubmit(true);
        new Button(container, "insert").setSubmit(true);
        new Button(container, "delete").setSubmit(true);
//        new Button(container, "firstpage").setSubmit(true);
//        new Button(container, "earlierpage").setSubmit(true);
//        new Button(container, "laterpage").setSubmit(true);
//        new Button(container, "lastpage").setSubmit(true);
        
        view.setNavbarActionEnabled("back", true);
        view.addContainer(container);
    }
    
    /**
     * 
     * @param controldata
     * @param view
     */
    public final void show(ControlData controldata, ViewData view) {
        String model = ((InputComponent)view.getElement("model.name")).
                getValue();
        
        controldata.clearParameters();
        controldata.addParameter("mode", "show");
        controldata.addParameter("view.type", Const.SINGLE);
        controldata.addParameter("model.name", model);
        controldata.setReloadableView(true);
        controldata.redirect(null, "select");
    }
}