package jmri.jmrit.display.configurexml;

import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.MemoryIcon;
import jmri.util.NamedBeanHandle;
import jmri.Memory;
import org.jdom.Attribute;
import org.jdom.Element;
import java.util.List;


/**
 * Handle configuration for display.MemoryIcon objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004
 * @version $Revision: 1.30 $
 */
public class MemoryIconXml extends PositionableLabelXml {

    public MemoryIconXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * MemoryIcon
     * @param o Object to store, of type MemoryIcon
     * @return Element containing the complete info
     */
    public Element store(Object o) {

        MemoryIcon p = (MemoryIcon)o;

        Element element = new Element("memoryicon");

        // include attributes
        element.setAttribute("memory", p.getMemory().getName());
        storeCommonAttributes(p, element);
        storeTextInfo(p, element);
        
        element.setAttribute("selectable", (p.isSelectable()?"yes":"no"));

        element.setAttribute("class", "jmri.jmrit.display.configurexml.MemoryIconXml");
        if (p.getDefaultIcon()!=null)
            element.setAttribute("defaulticon", p.getDefaultIcon().getURL());

		// include contents
		java.util.HashMap<String, NamedIcon> map = p.getMap();
		if (map!=null) {
		    java.util.Iterator<String> iterator = map.keySet().iterator();
    	    while (iterator.hasNext()) {
    		    String key = iterator.next().toString();
    		    String value = map.get(key).getName();
    		    Element e2 = new Element("memorystate");
    		    e2.setAttribute("value", key);
    		    e2.setAttribute("icon", value);
    		    element.addContent(e2);
    	    }
        }
        return element;
    }


    public boolean load(Element element) {
        log.error("Invalid method called");
        return false;
    }

    /**
     * Load, starting with the memoryicon element, then
     * all the value-icon pairs
     * @param element Top level Element to unpack.
     * @param o an Editor as an Object
     */
    @SuppressWarnings("unchecked")
	public void load(Element element, Object o) {

        String name;
        Attribute attr = element.getAttribute("memory"); 
        if (attr == null) {
            log.error("incorrect information for a memory location; must use memory name");
            return;
        } else {
            name = attr.getValue();
        }

		Editor ed = null;
        MemoryIcon l;
		if (o instanceof LayoutEditor) {
			ed = (LayoutEditor) o;
            l = new jmri.jmrit.display.layoutEditor.MemoryIcon("   ", (LayoutEditor)ed);
            ((LayoutEditor)ed).memoryLabelList.add((jmri.jmrit.display.layoutEditor.MemoryIcon)l);
		}
		else if (o instanceof jmri.jmrit.display.Editor) {
			ed = (Editor) o;
            l = new MemoryIcon("", ed);
        }
        else {
			log.error("Unrecognizable class - "+o.getClass().getName());
            return;
		}

        Memory m = jmri.InstanceManager.memoryManagerInstance().getMemory(name);
        if (m!=null) {
            l.setMemory(new NamedBeanHandle<Memory>(name, m));
        } else {
            log.error("Memory named '"+attr.getValue()+"' not found.");
            return;
        }
        

        loadTextInfo(l, element);
        
        Attribute a = element.getAttribute("selectable");
        if (a!=null && a.getValue().equals("yes")) l.setSelectable(true);
        else l.setSelectable(false);
        
        // get the icon pairs
        List<Element> items = element.getChildren();
        for (int i = 0; i<items.size(); i++) {
            // get the class, hence the adapter object to do loading
            Element item = items.get(i);
            String icon = item.getAttribute("icon").getValue();
            String keyValue = item.getAttribute("value").getValue();
        	l.addKeyAndIcon(NamedIcon.getIconByName(icon), keyValue);
		}
        ed.putItem(l);
        // load individual item's option settings after editor has set its global settings
        loadCommonAttributes(l, Editor.MEMORIES, element);
        l.displayState();
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MemoryIconXml.class.getName());
}