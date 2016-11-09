/*
 *
 * @author Ming-Jheng Li
 *
 *
 * Copyright 2015 Ming-Jheng Li
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package tw.edu.ym.lab525.controller;

import static com.google.common.collect.Lists.newArrayList;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.util.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.wnameless.workbookaccessor.WorkbookWriter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import net.sf.rubycollect4j.RubyFile;
import tw.edu.ym.lab525.unit.TimeStamp;

@Controller
public class MainController {

  @RequestMapping(value = "/view", method = RequestMethod.GET)
  public String queryMethod(ModelMap map,
      @RequestParam(value = "amount") Integer amount,
      @RequestParam Map<String, String> queryParameters,
      @RequestParam MultiValueMap<String, String> multiMap) {
    if (queryParameters.get("amount").equals("")) {
      amount = 5;
    }
    List<Integer> amounts = newArrayList();
    for (int i = 1; i <= amount; i++) {
      amounts.add(i);
    }
    queryParameters.remove("amount");
    map.addAttribute("amounts", amounts);
    map.addAttribute("guids", queryParameters);
    return "home";
  }

  @RequestMapping(value = "/downloadExcel", method = RequestMethod.POST)
  public void downloadExcel(HttpServletResponse response,
      @RequestParam Map<String, String> queryParameters,
      @RequestParam MultiValueMap<String, String> multiMap) throws IOException {
    Multimap<String, String> maps = ArrayListMultimap.create();
    WorkbookWriter writer = WorkbookWriter.openXLSX();
    writer.addRow("patient", "protocol no.", "tube no.");

    for (String key : queryParameters.keySet()) {
      if (!queryParameters.get(key).equals("")) {
        String guidProtocol = key.split("@")[0] + "@" + key.split("@")[1];
        maps.put(guidProtocol, queryParameters.get(key));
      }
    }

    for (String key : maps.keySet()) {
      String guid = key.split("@")[0];
      String protocol = key.split("@")[1];
      for (String tube : maps.get(key))
        writer.addRow(guid, protocol, tube);
    }

    String fileName = "STAMS_Import_" + TimeStamp.simpleDateTime() + ".xls";
    InputStream is = new FileInputStream(writer.save(fileName));
    IOUtils.copy(is, response.getOutputStream());
    response.setHeader("Content-disposition",
        "attachment; filename=" + fileName);
    response.flushBuffer();
    RubyFile.delete(fileName);
  }

}
