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
    
	private static Pattern headingPattern = Pattern.compile("</?h[1-6]");
    
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

    	Matcher m = headingPattern.matcher(included);
    	StringBuffer sb = new StringBuffer();
    	int lastHtagStart = 0;
    	while (m.find())
    	{
    		int off = m.start();
    		
    		// process <hN>...</hN> elements
    		if (included.charAt(off) == '<')
    		{
    			if (included.charAt(off+1) != '/')
    			{
    				int newLevel = included.charAt(off+2) - '0' + hshift;
    				if (newLevel > 6)
    					newLevel = 6; // XXX: better alternative might be to change to one-line strong paragraph

    				lastHtagStart = off; // record for capturing the section title (for anchoring)
    				
        			m.appendReplacement(sb, Matcher.quoteReplacement("<h" + newLevel));
    			}
    			else
    			{
    				int newLevel = included.charAt(off+3) - '0' + hshift;
    				if (newLevel > 6)
    					newLevel = 6; // XXX: better alternative might be to change to one-line strong paragraph 

        			// create an anchor so that the heading is link-able and appears in the TOC
        			//
        			int titleStart = included.indexOf('>', lastHtagStart) + 1;
        			String sectionTitle = included.substring(titleStart, off);
        			
        			String idAttr = " id='"
        						  + "S" + hshift + "-"
        						  + makeSectionAnchorName(
        								  context.getPageContext().getPageTitle(),
        					              sectionTitle)
        					      + "'"
        					      ;
        			
        			// insert id= within previous <hN> tag; this is tricky as appendReplacement alters the matcher
        			// state.  This is workedaround by appending the id string in the wrong place (the text of the
        			// heading) to get the matcher iterators updated appropriately, then overwriting the string
        			// buffer with the correct string in the correct location (the header tag).
        			//
        			m.appendReplacement(sb, Matcher.quoteReplacement(idAttr + "</h" + newLevel));
        			
        			// shift the id= attribute to be inside the opening tag
        			//                     |::::|[-------------]
        			//  i.e. start with <hN>Title id="something"</hN>
        			//       end with   <hN id="something">Title</hN>
        			//                     [-------------]|::::|
        			int idInsertionPoint = sb.indexOf(">", sb.lastIndexOf("<h"+newLevel));
        			sb.replace(idInsertionPoint, idInsertionPoint + idAttr.length() + sectionTitle.length() + 1, idAttr + ">" + sectionTitle);
    			}
    		}
    	}
    	m.appendTail(sb);
    	
    	// TODO: use XhtmlContent to convert macro invocations after  
        // TODO: replacing nested ((ac:name="hshift">N))
        // TODO: with ((ac:name="hshift">N+hshift))
    	
    	return sb.toString();
    }

	private static Pattern linkSpecialChars = Pattern.compile("[ (){}\\[\\]\\/@-]");

    private String makeSectionAnchorName(String pageTitle, String sectionTitle)
    {
    	String anchorName = pageTitle + "-" + sectionTitle;
    	Matcher m = linkSpecialChars.matcher(anchorName);
    	StringBuffer rc = new StringBuffer();
    	int keepHypenAt = pageTitle.length();
    	while (m.find())
    	{
    		int off = m.start();
    		if (off == keepHypenAt)
    		{
    			m.appendReplacement(rc, "-");
    			continue;
    		}
    		char c = anchorName.charAt(off);
    		switch (c)
    		{
    		case ' ': m.appendReplacement(rc, ""); break;
    		default : m.appendReplacement(rc, "%"+Integer.toHexString(0x100 | c).substring(1)); break;
    		}
    	}
    	m.appendTail(rc);
    	return rc.toString();
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
