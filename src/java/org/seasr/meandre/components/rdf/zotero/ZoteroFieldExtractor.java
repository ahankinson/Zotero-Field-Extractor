/**
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * All rights reserved.
 *
 * Developed by:
 *
 * Automated Learning Group
 * National Center for Supercomputing Applications
 * http://www.seasr.org
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal with the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimers.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimers in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the names of Automated Learning Group, The National Center for
 *    Supercomputing Applications, or University of Illinois, nor the names of
 *    its contributors may be used to endorse or promote products derived from
 *    this Software without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * WITH THE SOFTWARE.
 */

package org.seasr.meandre.components.rdf.zotero;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.support.generic.io.ModelUtils;

import com.hp.hpl.jena.rdf.model.Model;

/**
 *  This class extracts the list of authors per entry from a Zotero RDF
 *
 * @author Xavier Llor&agrave;
 * @author Boris Capitanu
 */

@Component(
		creator = "Xavier Llor&agrave",
		description = "Extract the authors for each entry of a Zotero RDF",
		name = "Zotero Field Extractor",
		tags = "zotero, author, information extraction",
		rights = Licenses.UofINCSA,
		mode = Mode.compute,
		firingPolicy = FiringPolicy.all,
		baseURL = "meandre://seasr.org/components/foundry/"
)
public class ZoteroFieldExtractor extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
	        name = Names.PORT_REQUEST_DATA,
			description = "A map object containing the key elements of the request and the associated values" +
    			"<br>TYPE: org.seasr.datatypes.BasicDataTypes.BytesMap" +
    			"<br>TYPE: java.util.Map<java.lang.String, byte[]>"
	)
	protected static final String IN_REQUEST = Names.PORT_REQUEST_DATA;

    //------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
	        name = "OUT_FIELD_NAME",
			description = "A list of vectors containing the field values. There is one vector for" +
					      "Zotero entry" +
					      "<br>TYPE: java.util.List<java.util.Vector<java.lang.String>>"
	)
	protected static final String OUT_FIELD_LIST = "OUT_FIELD_NAME";


    //--------------------------------------------------------------------------------------------

    //------------------------------ PROPERTIES -----------------------------------------------------

	@ComponentProperty(
			name = "ZOTERO_FIELD_NAME",
			description = "The Zotero Field name to send to the output." +
				"<br />Possibilities include" +
				"<ul><li>author</li>" +
				"<li>title</li>" +
				"<li>date</li>" +
				"<li>doi</li>" +
				"<li>issn</li>" +
				"<li>volume</li>" +
				"<li>abstract</li>" +
				"<li>series</li>" +
				"<li>itemtype</li>",
			defaultValue = "author"
	)
	
	protected static final String OUT_FIELD_NAME = "ZOTERO_FIELD_NAME";
	//--------------------------------------------------------------------------------------------
	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
		console.finest("got here!");
	}

	@Override
    public void executeCallBack(ComponentContext cc) throws Exception {
		Map<String,byte[]> map = DataTypeParser.parseAsStringByteArrayMap(cc.getDataComponentFromInput(IN_REQUEST));
		String zoteroField = cc.getProperty(OUT_FIELD_NAME).trim().toLowerCase();
		console.info("zotero Field");
		console.info(zoteroField);
		List<Vector<String>> list = new LinkedList<Vector<String>>();

		for ( String sKey:map.keySet() ) {
			Model model = ModelUtils.getModel(map.get(sKey), null);
			List<Vector<String>> fieldList = ZoteroExtendedUtils.extractFields(model, zoteroField, cc.getLogger());
			
			console.info("got " + fieldList.size() + " elements");
            for (Vector<String> fields : fieldList) {
                for (String field : fields)
                    console.finest("field: " + field);
            }
			list.addAll(fieldList);
		}

		cc.pushDataComponentToOutput(OUT_FIELD_LIST, list);
	}

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }
}
