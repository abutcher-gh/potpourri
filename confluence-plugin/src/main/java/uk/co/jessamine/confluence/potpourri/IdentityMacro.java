package uk.co.jessamine.confluence.potpourri;

import java.util.Map;
import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.macro.MacroExecutionException;

public class IdentityMacro implements Macro
{
   @Override
   public String execute(Map<String, String> parameters, String body, ConversionContext context) throws MacroExecutionException
   {
      if (body.startsWith("<p>") && body.endsWith("</p>"))
         switch (body.charAt(3))
         {
         case ' ':
         case '\u00a0':
            return "";
         default:
            return body.substring(3, body.length()-4);
         }
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
