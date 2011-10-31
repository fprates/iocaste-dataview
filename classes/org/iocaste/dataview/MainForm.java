package org.iocaste.dataview;

import java.lang.reflect.Method;

import org.iocaste.documents.common.DocumentModel;
import org.iocaste.documents.common.DocumentModelItem;
import org.iocaste.documents.common.Documents;
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
import org.iocaste.shell.common.Shell;
import org.iocaste.shell.common.Table;
import org.iocaste.shell.common.TableItem;
import org.iocaste.shell.common.ViewData;

public class MainForm extends AbstractPage {
    
    public final void earlierpage(ControlData cdata, ViewData vdata) {
        
    }
    
    public final void edit(ControlData controldata, ViewData view) 
            throws Exception {
        String modelname = ((InputComponent)view.getElement("model.name")).
                getValue();
        Documents documents = new Documents(this);
        String query = new StringBuilder("select * from ").
                append(modelname).toString();
        
        controldata.clearParameters();
        controldata.addParameter("mode", "edit");
        controldata.addParameter("view.type", Const.SINGLE);
        controldata.addParameter("model.name", modelname);
        controldata.addParameter("model.regs", documents.select(query, null));
        controldata.setReloadableView(true);
        controldata.redirect(null, "select");
    }
    
    public final void firstpage(ControlData cdata, ViewData vdata) {
        
    }
    
    public final void lastpage(ControlData cdata, ViewData vdata) {
        
    }
    
    public final void laterpage(ControlData cdata, ViewData vdata) {
        
    }

    public void main(ViewData view) {
        Container container = new Form(null, "main");
        DataForm form = new DataForm(container, "model");
        DataItem formitem = new DataItem(form, Const.TEXT_FIELD, "model.name");
        
        formitem.setObligatory(true);
        form.addAction("edit");
        form.addAction("show");
        
        view.setFocus("model.name");
        view.setTitle("dataview.selection");
        view.addContainer(container);
    }
    
    public final void save(ControlData cdata, ViewData vdata) throws Exception {
        TableItem tableitem;
        Object reg;
        String value;
        InputComponent input;
        DocumentModelItem modelitem;
        Method method;
        boolean modifyok;
        Element cell;
        String modelname = (String)vdata.getParameter("model.name");
        Documents documents = new Documents(this);
        DocumentModel model = documents.getModel(modelname);
        Table table = ((Table)vdata.getElement(modelname));
        
        for (Element element : table.getElements()) {
            if (element.getType() != Const.TABLE_ITEM)
                continue;
            
            tableitem = (TableItem)element;
            reg = Class.forName(model.getClassName()).newInstance();
            modifyok = false;
            
            for (String name : tableitem.getElementNames()) {
                cell = table.getElement(name);
                
                if (!cell.isDataStorable())
                    continue;
                
                input = (InputComponent)cell;
                modelitem = input.getModelItem();
                
                value = input.getValue();
                if (value == null && model.isKey(modelitem)) {
                	modifyok = false;
                	break;
                }
                
                method = reg.getClass().getMethod(modelitem.getSetterName(),
                        modelitem.getDataElement().getClassType());
                method.invoke(reg, input.getParsedValue());
                modifyok = true;
            }
            
            if (modifyok)
            	documents.modify(model, reg);
        }
    }
    
    public void select(ViewData view) throws Exception {
        Container container = new Form(null, "dataview.container");
        DataItem dataitem;
        TableItem tableitem;
        String name;
        StringBuilder sb;
        Element tfield;
        Element element;
        Element[] elements;
        boolean key;
        int i = 0;
        Object[] itens = (Object[])view.getParameter("model.regs");
        Documents documents = new Documents(this);
        DocumentModel model = documents.getModel(
                (String)view.getParameter("model.name"));
        Table table = new Table(container, 0, model.getName());
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
        
        for (int k = 0; k < 20; k++) {
            sb = new StringBuilder(table.getName()).append(".").append(k);
            name = sb.toString();
            tableitem = new TableItem(table, name);
            
            i = 0;
            for (DocumentModelItem modelitem : model.getItens()) {
                element = elements[i++];
                if (element.getType() != Const.DATA_ITEM)
                    continue;
                
                sb.setLength(0);
                dataitem = (DataItem)element;
                dataitem.setModelItem(modelitem);
                
                tfield = Shell.createInputItem(table, dataitem,
                        sb.append(name).append(".").append(element.getName()).
                        toString());
                tfield.setEnabled(!model.isKey(modelitem));
                
                tableitem.add(tfield);
                
                if (k < itens.length)
                    Shell.moveItemToInput((InputComponent)tfield, itens[k]);
            }
        }
        
        new Button(container, "save").setSubmit(true);
        new Button(container, "firstpage").setSubmit(true);
        new Button(container, "earlierpage").setSubmit(true);
        new Button(container, "laterpage").setSubmit(true);
        new Button(container, "lastpage").setSubmit(true);
        
        view.addContainer(container);
    }
    
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
