package uk.co.jessamine.confluence.potpourri;

import java.util.Map;
import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.macro.MacroExecutionException;

public class DontDisplayMacro implements Macro
{
   @Override
   public String execute(Map<String, String> parameters, String body, ConversionContext context) throws MacroExecutionException
   {
      return "";
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
