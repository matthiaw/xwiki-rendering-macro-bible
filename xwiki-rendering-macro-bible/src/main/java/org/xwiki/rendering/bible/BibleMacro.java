/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.rendering.bible;

import javax.inject.Inject;
import javax.inject.Named;



import java.util.List;
import java.util.Arrays;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.block.ParagraphBlock;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.MacroContentParser;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.transformation.MacroTransformationContext;

@Component
@Named("bible")
public class BibleMacro extends AbstractMacro<BibleMacroParameters>
{
    /**
     * The description of the macro.
     */
    private static final String DESCRIPTION = "Bible Macro";
    
    /**
     * Create and initialize the descriptor of the macro.
     */
    public BibleMacro()
    {
        super("Bible", DESCRIPTION, BibleMacroParameters.class);
    }
    
    @Inject
	private MacroContentParser contentParser;

    @Override
    public List<Block> execute(BibleMacroParameters parameters, String content, MacroTransformationContext context)
        throws MacroExecutionException
    {
        List<Block> result;

        BibleLibrary.initialize();
        
        String key = parameters.getKey();
        String verse = BibleLibrary.getCanonicalText(key);
        
//        List<Block> wordBlockAsList = Arrays.<Block>asList(new WordBlock(verse));
        
        result = this.contentParser.parse("//"+verse+"// ,,"+key.replace("#", " ")+",,", context, true, context.isInline()).getChildren();
        
        return result;
    }

    @Override
    public boolean supportsInlineMode()
    {
        return true;
    }
}
