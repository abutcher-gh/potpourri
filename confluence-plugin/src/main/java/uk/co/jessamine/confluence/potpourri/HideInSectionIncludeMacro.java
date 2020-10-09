package uk.co.jessamine.confluence.potpourri;

import java.util.Map;
import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.macro.MacroExecutionException;

/**
 * Actually this is just an identity macro.  It shows exactly what it contains.
 * It is replaced by SectionIncludeMacro with a DontDisplayMacro when included.
 */
public class HideInSectionIncludeMacro implements Macro
{
   @Override
   public String execute(Map<String, String> parameters, String body, ConversionContext context) throws MacroExecutionException
   {
      return body;
   }

   @Override
   public BodyType getBodyType()
   {
      return BodyType.RICH_TEXT;
   }

   @Override
   public OutputType getOutputType()
   {
      return OutputType.INLINE;
   }
}
