package uk.co.jessamine.confluence.potpourri;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.xhtml.api.XhtmlContent;

public class SectionIncludeMacro implements Macro
{
    private final PageManager pageManager;
    private final XhtmlContent xhtmlUtils;
    
    public SectionIncludeMacro(PageManager pageManager, XhtmlContent xhtmlUtils)
    {
        this.pageManager = pageManager;
        this.xhtmlUtils = xhtmlUtils;
    }

    private static String error(String error)
    {
    	return "<p><strong>SectionIncludeMacro: <em>Error:</em></strong> " + error + "</p>";
    }
    
	private static Pattern patterns = Pattern.compile
			("(</?h[1-6]|ac:name=\"hshift\">[1-5]|c:name=\"(toc|toc-zone)\")");
    
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
    	boolean removeToc = true;
    	{
    	   	String removeTocParam = parameters.get("remove-toc");
        	if (removeTocParam != null)
        		removeToc = Boolean.parseBoolean(removeTocParam);
    	}

    	String included = page.getBodyAsString();

    	Matcher m = patterns.matcher(included);
    	StringBuffer sb = new StringBuffer();
    	while (m.find())
    	{
    		int off = m.start();
    		char first = included.charAt(off);
    		
    		// process <hN>...</hN> elements
    		if (first == '<')
    		{
    			if (included.charAt(off+1) != '/')
    			{
    				int newLevel = included.charAt(off+2) - '0' + hshift;
    				if (newLevel > 6)
    					newLevel = 6; // XXX: better alternative might be to change to one-line strong paragraph

        			m.appendReplacement(sb, Matcher.quoteReplacement("<h" + newLevel));
    			}
    			else
    			{
    				int newLevel = included.charAt(off+3) - '0' + hshift;
    				if (newLevel > 6)
    					newLevel = 6; // XXX: better alternative might be to change to one-line strong paragraph 

        			m.appendReplacement(sb, Matcher.quoteReplacement("<span class='hidden'>S" + hshift + "</span></h" + newLevel));
    			}
    		}
    		else if (first == 'a')
    		{
    			// 012345678901234567
    			// ac:name="hshift">N
    			
				int newLevel = included.charAt(off+17) - '0' + hshift;
    			
    			m.appendReplacement(sb, Matcher.quoteReplacement("ac:name=\"hshift\">" + newLevel));
    		}
    		else if (removeToc && first == 'c')
    		{
    			m.appendReplacement(sb, Matcher.quoteReplacement("c:name=\"identity\""));
    		}
    	}
    	m.appendTail(sb);
    	
    	try {
    		return xhtmlUtils.convertStorageToView(sb.toString(), context);
    	}
    	catch (Exception e) {
    		return error(e.toString());
    	}
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
