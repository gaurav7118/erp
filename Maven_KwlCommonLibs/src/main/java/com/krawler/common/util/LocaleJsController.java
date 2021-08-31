/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.krawler.common.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.mvc.multiaction.InternalPathMethodNameResolver;
import org.springframework.web.servlet.mvc.multiaction.MethodNameResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

public class LocaleJsController extends AbstractController implements MessageSourceAware {

    private MessageSource ms;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        OutputStream out = response.getOutputStream();
        response.setContentType("text/javascript");
        byte[] buffer = getContents(RequestContextUtils.getLocale(request));
        out.write(buffer);
        out.close();
        return null;
    }

    private byte[] getContents(Locale locale) {
        Object[] params = new String[]{"{0}", "{1}", "{2}", "{3}", "{4}", "{5}", "{6}", "{7}", "{8}", "{9}", "{10}"};

        String str =
                "Wtf.UpdateManager.defaults.indicatorText = \"" + this.ms.getMessage("updatemanager.indicator", params, "", locale) + "\";"
                + "if(Wtf.View){"
                + "   Wtf.View.prototype.emptyText = \"" + this.ms.getMessage("view.empty", params, "", locale) + "\";"
                + "}"
                + "if(Wtf.grid.GridPanel){"
                + "   Wtf.grid.GridPanel.prototype.ddText = \"" + this.ms.getMessage("grid.dd.text", params, "", locale) + "\";"
                + "}"
                + "if(Wtf.TabPanelItem){"
                + "   Wtf.TabPanelItem.prototype.closeText = \"" + this.ms.getMessage("", params, "", locale) + "\";"
                + "}"
                + "if(Wtf.form.Field){"
                + "   Wtf.form.Field.prototype.invalidText = \"" + this.ms.getMessage("field.invalid", params, "", locale) + "\";"
                + "}"
                + "if(Wtf.LoadMask){"
                + "    Wtf.LoadMask.prototype.msg = \"" + this.ms.getMessage("loadmask.msg", params, "", locale) + "\";"
                + "}"
                + //		"Date.monthNames = ["+
                //			"\""+this.ms.getMessage("month.january", params, "", locale)+"\","+
                //			"\""+this.ms.getMessage("month.february", params, "", locale)+"\","+
                //		  "\""+this.ms.getMessage("month.march", params, "", locale)+"\","+
                //		  "\""+this.ms.getMessage("month.april", params, "", locale)+"\","+
                //		  "\""+this.ms.getMessage("month.may", params, "", locale)+"\","+
                //		  "\""+this.ms.getMessage("month.june", params, "", locale)+"\","+
                //		  "\""+this.ms.getMessage("month.july", params, "", locale)+"\","+
                //		  "\""+this.ms.getMessage("month.august", params, "", locale)+"\","+
                //		  "\""+this.ms.getMessage("month.september", params, "", locale)+"\","+
                //		  "\""+this.ms.getMessage("month.october", params, "", locale)+"\","+
                //		  "\""+this.ms.getMessage("month.november", params, "", locale)+"\","+
                //		  "\""+this.ms.getMessage("month.december", params, "", locale)+"\""+
                //		"];"+
                //
                //		"Date.getShortMonthName = function(month) {"+
                //		"  return Date.monthNames[month].substring(0, 3);"+
                //		"};"+
                //
                //		"Date.monthNumbers = {"+
                //		"  \""+getPart(this.ms.getMessage("month.january", params, "", locale),0,3)+"\" : 0,"+
                //		"  \""+getPart(this.ms.getMessage("month.february", params, "", locale),0,3)+"\" : 1,"+
                //		"  \""+getPart(this.ms.getMessage("month.march", params, "", locale),0,3)+"\" : 2,"+
                //		"  \""+getPart(this.ms.getMessage("month.april", params, "", locale),0,3)+"\" : 3,"+
                //		"  \""+getPart(this.ms.getMessage("month.may", params, "", locale),0,3)+"\" : 4,"+
                //		"  \""+getPart(this.ms.getMessage("month.june", params, "", locale),0,3)+"\" : 5,"+
                //		"  \""+getPart(this.ms.getMessage("month.july", params, "", locale),0,3)+"\" : 6,"+
                //		"  \""+getPart(this.ms.getMessage("month.august", params, "", locale),0,3)+"\" : 7,"+
                //		"  \""+getPart(this.ms.getMessage("month.september", params, "", locale),0,3)+"\" : 8,"+
                //		"  \""+getPart(this.ms.getMessage("month.october", params, "", locale),0,3)+"\" : 9,"+
                //		"  \""+getPart(this.ms.getMessage("month.november", params, "", locale),0,3)+"\" : 10,"+
                //		"  \""+getPart(this.ms.getMessage("month.december", params, "", locale),0,3)+"\" : 11"+
                //		"};"+
                //
                //		"Date.getMonthNumber = function(name) {"+
                //		"  return Date.monthNumbers[name.substring(0, 3)];"+
                //		"};"+
                //
                //		"Date.dayNames = ["+
                //		   "\""+this.ms.getMessage("day.sunday", params, "", locale)+"\","+
                //		   "\""+this.ms.getMessage("day.monday", params, "", locale)+"\","+
                //		   "\""+this.ms.getMessage("day.tuesday", params, "", locale)+"\","+
                //		   "\""+this.ms.getMessage("day.wednesday", params, "", locale)+"\","+
                //		   "\""+this.ms.getMessage("day.thursday", params, "", locale)+"\","+
                //		   "\""+this.ms.getMessage("day.friday", params, "", locale)+"\","+
                //		   "\""+this.ms.getMessage("day.saturday", params, "", locale)+"\""+
                //		"];"+
                //
                //		"Date.getShortDayName = function(day) {"+
                //		"  return Date.dayNames[day].substring(0, 3);"+
                //		"};"+
                "if(Wtf.MessageBox){"
                + "   Wtf.MessageBox.buttonText = {"
                + "      ok     : \"" + this.ms.getMessage("button.ok", params, "", locale) + "\","
                + "      cancel : \"" + this.ms.getMessage("button.cancel", params, "", locale) + "\","
                + "      yes    : \"" + this.ms.getMessage("button.yes", params, "", locale) + "\","
                + "      no     : \"" + this.ms.getMessage("button.no", params, "", locale) + "\""
                + "   };"
                + "}"
                + "if(Wtf.util.Format){"
                + "   Wtf.util.Format.date = function(v, format){"
                + "      if(!v) return \"\";"
                + "      if(!(v instanceof Date)) v = new Date(Date.parse(v));"
                + "      return v.dateFormat(format || \"d.m.Y\");"
                + "   };"
                + "}"
                + "if(Wtf.DatePicker){"
                + "   Wtf.apply(Wtf.DatePicker.prototype, {"
                + "      todayText         : \"" + this.ms.getMessage("datepicker.today", params, "", locale) + "\","
                + "      minText           : \"" + this.ms.getMessage("datepicker.min", params, "", locale) + "\","
                + "      maxText           : \"" + this.ms.getMessage("datepicker.max", params, "", locale) + "\","
                + "      disabledDaysText  : \"" + this.ms.getMessage("datepicker.days.disabled", params, "", locale) + "\","
                + "      disabledDatesText : \"" + this.ms.getMessage("datepicker.dates.disabled", params, "", locale) + "\","
                + "      monthNames	: Date.monthNames,"
                + "      dayNames		: Date.dayNames,"
                + "      nextText          : \"" + this.ms.getMessage("datepicker.next", params, "", locale) + "\","
                + "      prevText          : \"" + this.ms.getMessage("datepicker.prev", params, "", locale) + "\","
                + "      monthYearText     : \"" + this.ms.getMessage("datepicker.month.year", params, "", locale) + "\","
                + "      todayTip          : \"" + this.ms.getMessage("datepicker.today.tip", params, "", locale) + "\","
                + "      format            : \"" + this.ms.getMessage("datepicker.format", params, "", locale) + "\","
                + "      okText            : \"" + this.ms.getMessage("datepicker.ok", params, "", locale) + "\","
                + "      cancelText        : \"" + this.ms.getMessage("datepicker.cancel", params, "", locale) + "\","
                + "      startDay 		: \"" + this.ms.getMessage("datepicker.startday", params, "", locale) + "\"*1"
                + "   });"
                + "}"
                + "if(Wtf.PagingToolbar){"
                + "   Wtf.apply(Wtf.PagingToolbar.prototype, {"
                + "      beforePageText : \"" + this.ms.getMessage("toolbar.paging.before.page", params, "", locale) + "\","
                + "      afterPageText  : \"" + this.ms.getMessage("toolbar.paging.after.page", params, "", locale) + "\","
                + "      firstText      : \"" + this.ms.getMessage("toolbar.paging.first", params, "", locale) + "\","
                + "      prevText       : \"" + this.ms.getMessage("toolbar.paging.prev", params, "", locale) + "\","
                + "      nextText       : \"" + this.ms.getMessage("toolbar.paging.next", params, "", locale) + "\","
                + "      lastText       : \"" + this.ms.getMessage("toolbar.paging.last", params, "", locale) + "\","
                + "      refreshText    : \"" + this.ms.getMessage("toolbar.paging.refresh", params, "", locale) + "\","
                + "      displayMsg     : \"" + this.ms.getMessage("toolbar.paging.display.msg", params, "", locale) + "\","
                + "      emptyMsg       : \"" + this.ms.getMessage("toolbar.paging.empty.msg", params, "", locale) + "\""
                + "   });"
                + "}"
                + "if(Wtf.form.TextField){"
                + "   Wtf.apply(Wtf.form.TextField.prototype, {"
                + "      minLengthText : \"" + this.ms.getMessage("field.text.length.min", params, "", locale) + "\","
                + "      maxLengthText : \"" + this.ms.getMessage("field.text.length.max", params, "", locale) + "\","
                + "      blankText     : \"" + this.ms.getMessage("field.text.blank", params, "", locale) + "\","
                + "      regexText     : \"" + this.ms.getMessage("field.text.regex", params, "", locale) + "\","
                + "      emptyText     : \"" + this.ms.getMessage("field.text.empty", params, "", locale) + "\""
                + "   });"
                + "}"
                + "if(Wtf.form.NumberField){"
                + "   Wtf.apply(Wtf.form.NumberField.prototype, {"
                + "      minText : \"" + this.ms.getMessage("field.number.min", params, "", locale) + "\","
                + "      maxText : \"" + this.ms.getMessage("field.number.max", params, "", locale) + "\","
                + "      nanText : \"" + this.ms.getMessage("field.number.nan", params, "", locale) + "\""
                + "   });"
                + "}"
                + "if(Wtf.form.DateField){"
                + "   Wtf.apply(Wtf.form.DateField.prototype, {"
                + "      disabledDaysText  : \"" + this.ms.getMessage("field.date.disabled.days", params, "", locale) + "\","
                + "      disabledDatesText : \"" + this.ms.getMessage("field.date.disabled.dates", params, "", locale) + "\","
                + "      minText           : \"" + this.ms.getMessage("field.date.min", params, "", locale) + "\","
                + "      maxText           : \"" + this.ms.getMessage("field.date.max", params, "", locale) + "\","
                + "      invalidText       : \"" + this.ms.getMessage("field.date.invalid", params, "", locale) + "\","
                + "      format            : \"" + this.ms.getMessage("field.date.format", params, "", locale) + "\""
                + "   });"
                + "}"
                + "if(Wtf.form.ComboBox){"
                + "   Wtf.apply(Wtf.form.ComboBox.prototype, {"
                + "      loadingText       : \"" + this.ms.getMessage("field.combo.loading", params, "", locale) + "\","
                + "      valueNotFoundText : \"" + this.ms.getMessage("field.combo.value.not.found", params, "", locale) + "\""
                + "   });"
                + "}"
                + "if(Wtf.form.VTypes){"
                + "   Wtf.apply(Wtf.form.VTypes, {"
                + "      emailText    : \"" + this.ms.getMessage("validation.email", params, "", locale) + "\","
                + "      urlText      : \"" + this.ms.getMessage("validation.url", params, "", locale) + "\","
                + "      alphaText    : \"" + this.ms.getMessage("validation.alpha", params, "", locale) + "\","
                + "      alphanumText : \"" + this.ms.getMessage("validation.alphanum", params, "", locale) + "\""
                + "   });"
                + "}"
                + "if(Wtf.form.HtmlEditor){"
                + "  Wtf.apply(Wtf.form.HtmlEditor.prototype, {"
                + "    createLinkText : \"" + this.ms.getMessage("he.createlink", params, "", locale) + "\","
                + "    buttonTips : {"
                + "      bold : {"
                + "        title: \"" + this.ms.getMessage("he.button.bold.title", params, "", locale) + "\","
                + "        text: \"" + this.ms.getMessage("he.button.bold.text", params, "", locale) + "\","
                + "        cls: \"" + this.ms.getMessage("he.button.bold.cls", params, "", locale) + "\""
                + "      },"
                + "      italic : {"
                + "        title: \"" + this.ms.getMessage("he.button.italic.title", params, "", locale) + "\","
                + "        text: \"" + this.ms.getMessage("he.button.italic.text", params, "", locale) + "\","
                + "        cls: \"" + this.ms.getMessage("he.button.italic.cls", params, "", locale) + "\""
                + "      },"
                + "      underline : {"
                + "        title: \"" + this.ms.getMessage("he.button.underline.title", params, "", locale) + "\","
                + "        text: \"" + this.ms.getMessage("he.button.underline.text", params, "", locale) + "\","
                + "        cls: \"" + this.ms.getMessage("he.button.underline.cls", params, "", locale) + "\""
                + "      },"
                + "      increasefontsize : {"
                + "        title: \"" + this.ms.getMessage("he.button.increasefontsize.title", params, "", locale) + "\","
                + "        text: \"" + this.ms.getMessage("he.button.increasefontsize.text", params, "", locale) + "\","
                + "        cls: \"" + this.ms.getMessage("he.button.increasefontsize.cls", params, "", locale) + "\""
                + "      },"
                + "      decreasefontsize : {"
                + "        title: \"" + this.ms.getMessage("he.button.decreasefontsize.title", params, "", locale) + "\","
                + "        text: \"" + this.ms.getMessage("he.button.decreasefontsize.text", params, "", locale) + "\","
                + "        cls: \"" + this.ms.getMessage("he.button.decreasefontsize.cls", params, "", locale) + "\""
                + "      },"
                + "      backcolor : {"
                + "        title: \"" + this.ms.getMessage("he.button.backcolor.title", params, "", locale) + "\","
                + "        text: \"" + this.ms.getMessage("he.button.backcolor.text", params, "", locale) + "\","
                + "        cls: \"" + this.ms.getMessage("he.button.backcolor.cls", params, "", locale) + "\""
                + "      },"
                + "      forecolor : {"
                + "        title: \"" + this.ms.getMessage("he.button.forecolor.title", params, "", locale) + "\","
                + "        text: \"" + this.ms.getMessage("he.button.forecolor.text", params, "", locale) + "\","
                + "        cls: \"" + this.ms.getMessage("he.button.forecolor.cls", params, "", locale) + "\""
                + "      },"
                + "      justifyleft : {"
                + "        title: \"" + this.ms.getMessage("he.button.justifyleft.title", params, "", locale) + "\","
                + "        text: \"" + this.ms.getMessage("he.button.justifyleft.text", params, "", locale) + "\","
                + "        cls: \"" + this.ms.getMessage("he.button.justifyleft.cls", params, "", locale) + "\""
                + "      },"
                + "      justifycenter : {"
                + "        title: \"" + this.ms.getMessage("he.button.justifycenter.title", params, "", locale) + "\","
                + "        text: \"" + this.ms.getMessage("he.button.justifycenter.text", params, "", locale) + "\","
                + "        cls: \"" + this.ms.getMessage("he.button.justifycenter.cls", params, "", locale) + "\""
                + "      },"
                + "      justifyright : {"
                + "        title: \"" + this.ms.getMessage("he.button.justifyright.title", params, "", locale) + "\","
                + "        text: \"" + this.ms.getMessage("he.button.justifyright.text", params, "", locale) + "\","
                + "        cls: \"" + this.ms.getMessage("he.button.justifyright.cls", params, "", locale) + "\""
                + "      },"
                + "      insertunorderedlist : {"
                + "        title: \"" + this.ms.getMessage("he.button.insertunorderedlist.title", params, "", locale) + "\","
                + "        text: \"" + this.ms.getMessage("he.button.insertunorderedlist.text", params, "", locale) + "\","
                + "        cls: \"" + this.ms.getMessage("he.button.insertunorderedlist.cls", params, "", locale) + "\""
                + "      },"
                + "      insertorderedlist : {"
                + "        title: \"" + this.ms.getMessage("he.button.insertorderedlist.title", params, "", locale) + "\","
                + "        text: \"" + this.ms.getMessage("he.button.insertorderedlist.text", params, "", locale) + "\","
                + "        cls: \"" + this.ms.getMessage("he.button.insertorderedlist.cls", params, "", locale) + "\""
                + "      },"
                + "      createlink : {"
                + "        title: \"" + this.ms.getMessage("he.button.createlink.title", params, "", locale) + "\","
                + "        text: \"" + this.ms.getMessage("he.button.createlink.text", params, "", locale) + "\","
                + "        cls: \"" + this.ms.getMessage("he.button.createlink.cls", params, "", locale) + "\""
                + "      },"
                + "      sourceedit : {"
                + "        title: \"" + this.ms.getMessage("he.button.sourceedit.title", params, "", locale) + "\","
                + "        text: \"" + this.ms.getMessage("he.button.sourceedit.text", params, "", locale) + "\","
                + "        cls: \"" + this.ms.getMessage("he.button.sourceedit.cls", params, "", locale) + "\""
                + "      }"
                + "    }"
                + "  });"
                + "}"
                + "if(Wtf.grid.GridView){"
                + "   Wtf.apply(Wtf.grid.GridView.prototype, {"
                + "      sortAscText  : \"" + this.ms.getMessage("grid.view.sort.asc", params, "", locale) + "\","
                + "      sortDescText : \"" + this.ms.getMessage("grid.view.sort.desc", params, "", locale) + "\","
                + "      lockText     : \"" + this.ms.getMessage("", params, "", locale) + "\","
                + "      unlockText   : \"" + this.ms.getMessage("", params, "", locale) + "\","
                + "      columnsText  : \"" + this.ms.getMessage("grid.view.columns", params, "", locale) + "\""
                + "   });"
                + "}"
                + "if(Wtf.grid.GroupingView){"
                + "  Wtf.apply(Wtf.grid.GroupingView.prototype, {"
                + "    emptyGroupText : \"" + this.ms.getMessage("grid.view.group.emptygroup", params, "", locale) + "\","
                + "    groupByText    : \"" + this.ms.getMessage("grid.view.group.groupby", params, "", locale) + "\","
                + "    showGroupsText : \"" + this.ms.getMessage("grid.view.group.showgroups", params, "", locale) + "\""
                + "  });"
                + "}"
                + "if(Wtf.grid.PropertyColumnModel){"
                + "   Wtf.apply(Wtf.grid.PropertyColumnModel.prototype, {"
                + "      nameText   : \"" + this.ms.getMessage("grid.cm.property.name", params, "", locale) + "\","
                + "      valueText  : \"" + this.ms.getMessage("grid.cm.property.value", params, "", locale) + "\","
                + "      dateFormat : \"" + this.ms.getMessage("grid.cm.property.dateformat", params, "", locale) + "\""
                + "   });"
                + "}"
                + "if(Wtf.layout.BorderLayout.SplitRegion){"
                + "   Wtf.apply(Wtf.layout.BorderLayout.SplitRegion.prototype, {"
                + "      splitTip            : \"" + this.ms.getMessage("layout.split.tip", params, "", locale) + "\","
                + "      collapsibleSplitTip : \"" + this.ms.getMessage("layout.split.tip.collapsible", params, "", locale) + "\""
                + "   });"
                + "}";
        return str.getBytes();
    }

    private String getPart(String str, int beginIndex, int endIndex) {
        return StringEscapeUtils.escapeJavaScript(StringEscapeUtils.unescapeJavaScript(str).substring(beginIndex, Math.min(endIndex, StringEscapeUtils.unescapeJavaScript(str).length())));
    }

    @Override
    public void setMessageSource(MessageSource messageSource) {
        ms = new EscapedMessageSource(messageSource);
    }

    private static class EscapedMessageSource implements MessageSource {

        MessageSource ms;

        public EscapedMessageSource(MessageSource messageSource) {
            this.ms = messageSource;
        }

        @Override
        public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
            return StringEscapeUtils.escapeJavaScript(ms.getMessage(resolvable, locale));
        }

        @Override
        public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
            return StringEscapeUtils.escapeJavaScript(ms.getMessage(code, args, locale));
        }

        @Override
        public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
            return StringEscapeUtils.escapeJavaScript(ms.getMessage(code, args, defaultMessage, locale));
        }
    }
}
