(function(Handsontable) {
	
	var DatetimeEditor = Handsontable.editors.BaseEditor.prototype.extend();
	var $DatetimeEditor = DatetimeEditor;
	var that = this;
	var CoreInstance = null;
	
	DatetimeEditor.prototype.init = function() {
		CoreInstance = this.instance;
		this.createElements();
		this.instance.addHook('afterDestroy', function() {
		    that.destroy();
		});
	};

	DatetimeEditor.prototype.createElements = function() {
		this.DatetimeInput = document.createElement('input');
		this.DatetimeInput.classList.add('handsontableDatetimeInput');
		this.DatetimeInput.classList.add('handsontableInput');
		this.DatetimeInputStyle = this.DatetimeInput.style;
		this.DatetimeInputStyle.width = 50;
		this.DatetimeInputStyle.height = 23 + 'px';
		this.DatetimeInputParent = document.createElement('div');
		this.DatetimeInputParent.classList.add('handsontableDatetimeInputHolder');
		this.DatetimeInputParentStyle = this.DatetimeInputParent.style;
		this.DatetimeInputParentStyle.position = 'absolute';
		this.DatetimeInputParentStyle.zindex = 100;
		this.DatetimeInputParentStyle.top = 0;
		this.DatetimeInputParentStyle.left = 0;
		this.DatetimeInputParentStyle.height = 24;
		this.DatetimeInputParentStyle.display = 'none';
		this.DateFormat = 'yy-mm-dd';
		this.TimeFormat = 'HH:mm:ss';

		$(this.DatetimeInputParent).append(this.DatetimeInput);
		$(this.instance.rootElement).append(this.DatetimeInputParent);

	};

	DatetimeEditor.prototype.prepare = function(row, col, prop, td,
			originalValue, cellProperties) {
		this._opened = false;
		$traceurRuntime.superGet(this, $DatetimeEditor.prototype, "prepare")
				.call(this, row, col, prop, td, originalValue, cellProperties);
		
		if(typeof this.cellProperties.dateFormat != "undefined" &&
				this.cellProperties.dateFormat != null)	
			this.DateFormat = this.cellProperties.dateFormat;
		if(typeof this.cellProperties.timeFormat != "undefined" &&
				this.cellProperties.timeFormat != null)
			this.TimeFormat = this.cellProperties.timeFormat;
		
		$(this.DatetimeInput).datetimepicker({
			dateFormat : this.DateFormat,
			timeFormat : this.TimeFormat,
			beforeShow:function(input) {
	            $(input).css({
	            	position : 'absolute',
	                "z-index": 999999
	            });
	        }
		});

	};

	DatetimeEditor.prototype.open = function() {
		this.refreshDimensions();
		
		$('.ui-datepicker').on('mousedown', function(event) {
			var that = CoreInstance.getActiveEditor();
			
			var target = $(event.target);
			
			if(!target.is('.ui-datepicker-close')) {
				event.stopPropagation();
			} else {
				that.close();
				that.finishEditing();
				event.stopPropagation();
			}
		});	
		
		var value = this.getValue();
		if(!value || value.length == 0) {
			var now = new Date();
			var time = {
				hour: now.getHours(),
				minute: now.getMinutes(),
				second: now.getSeconds()
			};
			this.DatetimeInput.value = $.datepicker.formatDate(this.DateFormat, now) + " " + $.datepicker.formatTime(this.TimeFormat, time);
			
		}
				
		this.instance.addHook('beforeKeyDown', onBeforeKeyDown);
		
		$(this.DatetimeInput).on('keydown', this.DatetimeInput, datetimeInputKeyDown);

	};
	
	/*
	 * DatetimeInput element key event handler
	 */
	var datetimeInputKeyDown = function(event) {
		var that = CoreInstance.getActiveEditor();
						
		switch(event.keyCode) {
		case 13:
			that.close();
			that.finishEditing(false, false);
			break;
		case 27:
			that.originalValue = null;
			that.DatetimeInput.value = null;
			that.close();
			that.finishEditing(true, false);
			break;
		}
	}
	
	function formatTime(date) {
		var hours = date.getHours();
		var minutes = date.getMinutes();
		var seconds = date.getSeconds();
		
		if(hours < 10) 
			hours = "0" + hours;
		if(minutes < 10)
			minutes = "0" + minutes;
		if(seconds < 10)
			seconds = "0" + seconds;
		
		return hours + ":" + minutes + ":" + seconds;
		
	}

	DatetimeEditor.prototype.close = function() {
		this._opened = false;
	};
	
	
	DatetimeEditor.prototype.finishEditing = function() {
		var isCancelled = arguments[0] !== (void 0) ? arguments[0] : false;
	    var ctrlDown = arguments[1] !== (void 0) ? arguments[1] : false;
	    	    
	    this.originalValue = this.getValue();
	    
	    this.DatetimeInputParentStyle.display = 'none';
		this.instance.removeHook('beforeKeyDown', onBeforeKeyDown);
		$('.ui-datepicker').css('display', 'none');
		
		$(this.DatetimeInput).unbind('keydown', datetimeInputKeyDown);
		
	    $traceurRuntime.superGet(this, $DatetimeEditor.prototype, "finishEditing").call(this, isCancelled, ctrlDown);
	}
	
	DatetimeEditor.prototype.getValue = function() {
		return this.DatetimeInput.value;
	}

	DatetimeEditor.prototype.setValue = function(newValue) {
		$(this.DatetimeInput).val(newValue);
	}

	DatetimeEditor.prototype.focus = function() {
		this.DatetimeInput.focus();
	};

	DatetimeEditor.prototype.refreshDimensions = function() {
		if (this.state !== Handsontable.EditorState.EDITING) {
			return;
		}

		this.TD = this.getEditedCell();
		if (!this.TD) {
			this.close();
			return;
		}

		var width = Handsontable.dom.outerWidth(this.TD) + 1,
			height = Handsontable.dom.outerHeight(this.TD) + 1, 
			currentOffset = Handsontable.dom.offset(this.TD), 
			containerOffset = Handsontable.dom.offset(this.instance.rootElement), 
			scrollableContainer = Handsontable.dom.getScrollableElement(this.TD), 
			editTop = currentOffset.top - containerOffset.top - 1 - (scrollableContainer.scrollTop || 0), 
			editLeft = currentOffset.left - containerOffset.left - 1 - (scrollableContainer.scrollLeft || 0), 
			editorSection = this.checkEditorSection(), 
			cssTransformOffset;
		
		var settings = this.instance.getSettings();
		var rowHeadersCount = settings.rowHeaders ? 1 : 0;
		var colHeadersCount = settings.colHeaders ? 1 : 0;
		
		switch (editorSection) {
		case 'top':
			cssTransformOffset = getCssTransform(this.instance.view.wt.wtOverlays.topOverlay.clone.wtTable.holder.parentNode);
			break;
		case 'left':
			cssTransformOffset = getCssTransform(this.instance.view.wt.wtOverlays.leftOverlay.clone.wtTable.holder.parentNode);
			break;
		case 'top-left-corner':
			cssTransformOffset = getCssTransform(this.instance.view.wt.wtOverlays.topLeftCornerOverlay.clone.wtTable.holder.parentNode);
			break;
		case 'bottom-left-corner':
			cssTransformOffset = getCssTransform(this.instance.view.wt.wtOverlays.bottomLeftCornerOverlay.clone.wtTable.holder.parentNode);
			break;
		case 'bottom':
			cssTransformOffset = getCssTransform(this.instance.view.wt.wtOverlays.bottomOverlay.clone.wtTable.holder.parentNode);
			break;
		}
		if (this.instance.getSelected()[0] === 0) {
			editTop += 1;
		}
		if (this.instance.getSelected()[1] === 0) {
			editLeft += 1;
		}
		
		this.DatetimeInputParentStyle.top = editTop + 'px';
		this.DatetimeInputParentStyle.left = editLeft + 'px';
		this.DatetimeInputParentStyle.height = 24 + 'px';
		
		this.DatetimeInputStyle.height = (height - 1) + 'px';
		
		this.DatetimeInputParentStyle.display = 'block';
		$('.ui-datepicker').css('display', 'block');
	};

	DatetimeEditor.prototype.getEditedCell = function() {
		var editorSection = this.checkEditorSection(), editedCell;

		switch (editorSection) {
		case 'top':
			editedCell = this.instance.view.wt.wtOverlays.topOverlay.clone.wtTable
					.getCell({
						row : this.row,
						col : this.col
					});
			this.DatetimeInputParentStyle.zIndex = 101;
			break;
		case 'top-left-corner':
			editedCell = this.instance.view.wt.wtOverlays.topLeftCornerOverlay.clone.wtTable
					.getCell({
						row : this.row,
						col : this.col
					});
			this.DatetimeInputParentStyle.zIndex = 103;
			break;
		case 'bottom-left-corner':
			editedCell = this.instance.view.wt.wtOverlays.bottomLeftCornerOverlay.clone.wtTable
					.getCell({
						row : this.row,
						col : this.col
					});
			this.DatetimeInputParentStyle.zIndex = 103;
			break;
		case 'left':
			editedCell = this.instance.view.wt.wtOverlays.leftOverlay.clone.wtTable
					.getCell({
						row : this.row,
						col : this.col
					});
			this.DatetimeInputParentStyle.zIndex = 102;
			break;
		case 'bottom':
			editedCell = this.instance.view.wt.wtOverlays.bottomOverlay.clone.wtTable
					.getCell({
						row : this.row,
						col : this.col
					});
			this.DatetimeInputParentStyle.zIndex = 102;
			break;
		default:
			editedCell = this.instance.getCell(this.row, this.col);
			this.DatetimeInputParentStyle.zIndex = '';
			break;
		}

		return editedCell != -1 && editedCell != -2 ? editedCell : void 0;
	};
	
	/**
	 * Editor key event handler
	 */
	var onBeforeKeyDown = function onBeforeKeyDown(event) {
		var instance = this, that = instance.getActiveEditor(), ctrlDown;
		
		ctrlDown = (event.ctrlKey || event.metaKey) && !event.altKey;
		
		switch(event.keyCode) {
		case KEY_CODES.BACKSPACE:
		case KEY_CODES.END:
		case KEY_CODES.HOME:
		case KEY_CODES.DELETE:
		case KEY_CODES.SPACE:
			if (that.isInFullEditMode()) {
				if ((!that.isWaiting() && !that.allowKeyEventPropagation) || (!that.isWaiting() && that.allowKeyEventPropagation && !that.allowKeyEventPropagation(event.keyCode))) {
					Handsontable.dom.stopImmediatePropagation(event);
				}
			}
	
			event.preventDefault();
			break;
	    case KEY_CODES.A:
	    case KEY_CODES.X:
	    case KEY_CODES.C:
	    case KEY_CODES.V:
			if (ctrlDown) {
				Handsontable.dom.stopImmediatePropagation(event);
			}
			event.preventDefault();
			break;
		}
	}
	
	var KEY_CODES = {
			  MOUSE_LEFT: 1,
			  MOUSE_RIGHT: 3,
			  MOUSE_MIDDLE: 2,
			  BACKSPACE: 8,
			  COMMA: 188,
			  INSERT: 45,
			  DELETE: 46,
			  END: 35,
			  ENTER: 13,
			  ESCAPE: 27,
			  CONTROL_LEFT: 91,
			  COMMAND_LEFT: 17,
			  COMMAND_RIGHT: 93,
			  ALT: 18,
			  HOME: 36,
			  PAGE_DOWN: 34,
			  PAGE_UP: 33,
			  PERIOD: 190,
			  SPACE: 32,
			  SHIFT: 16,
			  CAPS_LOCK: 20,
			  TAB: 9,
			  ARROW_RIGHT: 39,
			  ARROW_LEFT: 37,
			  ARROW_UP: 38,
			  ARROW_DOWN: 40,
			  F1: 112,
			  F2: 113,
			  F3: 114,
			  F4: 115,
			  F5: 116,
			  F6: 117,
			  F7: 118,
			  F8: 119,
			  F9: 120,
			  F10: 121,
			  F11: 122,
			  F12: 123,
			  A: 65,
			  X: 88,
			  C: 67,
			  V: 86
			};

	Handsontable.editors.DatetimeEditor = DatetimeEditor;
	Handsontable.editors.registerEditor('datetime', DatetimeEditor);

}(Handsontable));