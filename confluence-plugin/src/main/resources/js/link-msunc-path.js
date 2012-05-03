(function(){
AJS.toInit(function($){
	
	Confluence.Templates.LinkBrowser.msuncPanel = function(opt_data, opt_sb) {
		  var output = opt_sb || new soy.StringBuilder();
		  output.append(''
					 +'<div class="input-field">'
					 +   '<label id="msunc-label" for="msunc-link">', soy.$$escapeHtml("UNC Path"), ':</label>'
					 +   '<input type="text" tabindex="0" class="text" id="msunc-link" size="60">'
					 +'</div>'
					 +'<div class="input-field">'
					 +   '<label id="msunc-result-label" for="msunc-result">&nbsp;</label>'
					 +   '<input type="text" class="text description" id="msunc-result" name="destination"'
					 +   '       readonly="readonly" size="60" style="border-width: 0px">'
					 +'</div>'
				  );
		  return opt_sb ? '' : output.toString();
		};

    // The Advanced tab registers itself when the Link Browser is created.
    AJS.bind("dialog-created.link-browser", function (e, linkBrowser) {

        var key = 'msunc',                   // This panel's key.
            linkFieldName = 'msunc-link',    // The ID of the link input element.
            errorFieldName = 'msunc-error',  // The ID of the error element.
            $linkField,                      // The jQueryfied link input element.
            $errorField,                     // The jQueryfied error field.
            $fileField,                      // The jQueryfied file URL field.
            thisPanel,                       // A reference to this panel.
            keyTimeout,                      // Used to tell when the user has stopped typing.


        updateFileURI = function () {
            $fileField.val($linkField.val().replace(/^\\\\/, 'file://///').replace(/\\/g, '/'));
            linkBrowser.setLink(Confluence.Link.makeExternalLink($fileField.val()));
        };

        tab = linkBrowser.tabs[key] = {

            createPanel: function (context) {
                thisPanel = context.baseElement;
                $linkField = thisPanel.find("#msunc-link");
                $fileField = thisPanel.find("#msunc-result");
                $errorField = thisPanel.find("div[name='msunc-error']");
                thisPanel.find("form").keydown(function(e) {
                    if(e.keyCode == 13 && !linkBrowser.isSubmitButtonEnabled()) {
                        e.preventDefault();
                    }
                });
                $linkField.keyup(function (e) {
                    clearTimeout(keyTimeout);
                    $errorField.text('');

                    if (!!$linkField.val()) {
                        keyTimeout = setTimeout(function () {
                            updateFileURI();
                        }, 200);
                    }
                });
            },

            setLink: function (text) {
            	$linkField.val(text.replace(/file:\/\/+/,'\\\\').replace(/\//g, '\\'));
                $linkField.keyup();
                $linkField.change();
            },
            
            // Called when the panel is selected
            onSelect: function () {
                linkBrowser.moveLocationPanel(thisPanel);

                var openedLink = this.openedLink;
                if (openedLink) {
                    tab.setLink(this.openedLink.getHref());
                }
                
                // Defer focus to after LB is shown, gets around AJS.Dialog tabindex issues
                setTimeout(function() {
                    $linkField.focus();
                    AJS.$(".msunc-panel label").css('width', '5em');
                });
            },

            // Called when this panel is no longer selected
            onDeselect: function () {
                linkBrowser.restoreLocationPanel();
            },

            handlesLink: function (linkObj) {
            	return linkObj.getHref().match('^file://+');
            }
        };
    });

});
})();

