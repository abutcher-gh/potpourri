package uk.co.jessamine.confluence.potpourri;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.pages.Page;

public class SectionIncludeMacro implements Macro
{
    private final PageManager pageManager;
    
    public SectionIncludeMacro(PageManager pageManager)
    {
        this.pageManager = pageManager;
    }

    private static String error(String error)
    {
    	return "<p><strong>SectionIncludeMacro: <em>Error:</em></strong> " + error + "</p>";
    }
    
    // TODO: match also ((ac:name="hshift">N))

	private static Pattern patterns = Pattern.compile("</?h[1-6]\\b");
    
    @Override
    public String execute(Map<String, String> parameters, String body, ConversionContext context) throws MacroExecutionException
    {
    	Page page;
    	{
    		String spaceKey = parameters.get("space");
    		if (spaceKey == null)
    			spaceKey = context.getSpaceKey();
    		String pageTitle = parameters.get("page");
        	if (pageTitle == null)
        		return error("No 'page' specified.");
        	page = pageManager.getPage(spaceKey, pageTitle);
        	if (page == null)
        		return error("Page '"+pageTitle+"' not found in space '"+spaceKey+"'");
    	}
    	int hshift = 0;
    	{
    	   	String hshiftParam = parameters.get("hshift");
        	if (hshiftParam != null)
        		hshift = Integer.parseInt(hshiftParam);
        }

    	String included = page.getBodyAsString();

    	Matcher m = patterns.matcher(included);
    	StringBuffer sb = new StringBuffer();
    	while (m.find())
    	{
    		int off = m.start();
    		
    		// process <hN>...</hN> elements
    		if (included.charAt(off) == '<')
    		{
    			if (included.charAt(off+1) == '/')
        			m.appendReplacement(sb, "</h" + Integer.toString(included.charAt(off+3) - '0' + hshift));
    			else
        			m.appendReplacement(sb, "<h" + Integer.toString(included.charAt(off+2) - '0' + hshift));
    		}
    		
            // TODO: replace nested ((ac:name="hshift">N))
            // TODO: with ((ac:name="hshift">N+hshift))
    	}
    	m.appendTail(sb);
        return sb.toString();
    }

    @Override
    public BodyType getBodyType()
    {
        return BodyType.NONE;
    }

    @Override
    public OutputType getOutputType()
    {
        return OutputType.BLOCK;
    }

}
