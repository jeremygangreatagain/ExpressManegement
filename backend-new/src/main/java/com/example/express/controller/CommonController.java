package com.example.express.controller;

import com.example.express.common.Result;
import com.example.express.entity.Store;
import com.example.express.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 公共数据控制器
 * 提供系统公共数据接口，如省市区数据等
 */
@RestController
@RequestMapping("/api/common")
public class CommonController {

  @Autowired
  private StoreService storeService;

  /**
   * 创建省份数据
   * 
   * @param provinceName 省份名称
   * @param cities       城市数据，二维数组，每行第一个元素是城市名，后面是区县名
   * @return 格式化后的省份数据
   */
  private Map<String, Object> createProvince(String provinceName, String[][] cities) {
    Map<String, Object> province = new HashMap<>();
    province.put("value", provinceName);
    province.put("label", provinceName);

    List<Map<String, Object>> cityList = new ArrayList<>();

    for (String[] cityData : cities) {
      if (cityData.length == 0)
        continue;

      Map<String, Object> city = new HashMap<>();
      city.put("value", cityData[0]);
      city.put("label", cityData[0]);

      if (cityData.length > 1) {
        List<Map<String, Object>> districtList = new ArrayList<>();
        for (int i = 1; i < cityData.length; i++) {
          Map<String, Object> district = new HashMap<>();
          district.put("value", cityData[i]);
          district.put("label", cityData[i]);
          districtList.add(district);
        }
        city.put("children", districtList);
      }

      cityList.add(city);
    }

    province.put("children", cityList);
    return province;
  }

  /**
   * 获取省市区数据
   * 
   * @return 省市区数据
   */
  @GetMapping("/regions")
  public Result<List<Map<String, Object>>> getRegions() {
    // 构建省市区数据
    List<Map<String, Object>> regions = buildRegionData();
    return Result.success(regions);
  }

  /**
   * 构建省市区数据
   * 这里使用硬编码方式提供一些示例数据
   * 实际项目中可以从数据库或其他服务获取
   * 
   * @return 省市区数据列表
   */
  private List<Map<String, Object>> buildRegionData() {
    List<Map<String, Object>> provinces = new ArrayList<>();

    // 北京市
    Map<String, Object> beijing = new HashMap<>();
    beijing.put("value", "北京市");
    beijing.put("label", "北京市");

    List<Map<String, Object>> beijingCities = new ArrayList<>();
    Map<String, Object> beijingCity = new HashMap<>();
    beijingCity.put("value", "北京市");
    beijingCity.put("label", "北京市");

    List<Map<String, Object>> beijingDistricts = new ArrayList<>();
    String[] bjDistricts = { "东城区", "西城区", "朝阳区", "海淀区", "丰台区", "石景山区", "门头沟区", "房山区", "通州区", "顺义区", "昌平区", "大兴区",
        "怀柔区", "平谷区", "密云区", "延庆区" };
    for (String district : bjDistricts) {
      Map<String, Object> districtMap = new HashMap<>();
      districtMap.put("value", district);
      districtMap.put("label", district);
      beijingDistricts.add(districtMap);
    }

    beijingCity.put("children", beijingDistricts);
    beijingCities.add(beijingCity);
    beijing.put("children", beijingCities);
    provinces.add(beijing);

    // 上海市
    Map<String, Object> shanghai = new HashMap<>();
    shanghai.put("value", "上海市");
    shanghai.put("label", "上海市");

    List<Map<String, Object>> shanghaiCities = new ArrayList<>();
    Map<String, Object> shanghaiCity = new HashMap<>();
    shanghaiCity.put("value", "上海市");
    shanghaiCity.put("label", "上海市");

    List<Map<String, Object>> shanghaiDistricts = new ArrayList<>();
    String[] shDistricts = { "黄浦区", "徐汇区", "长宁区", "静安区", "普陀区", "虹口区", "杨浦区", "浦东新区", "闵行区", "宝山区", "嘉定区", "金山区", "松江区",
        "青浦区", "奉贤区", "崇明区" };
    for (String district : shDistricts) {
      Map<String, Object> districtMap = new HashMap<>();
      districtMap.put("value", district);
      districtMap.put("label", district);
      shanghaiDistricts.add(districtMap);
    }

    shanghaiCity.put("children", shanghaiDistricts);
    shanghaiCities.add(shanghaiCity);
    shanghai.put("children", shanghaiCities);
    provinces.add(shanghai);

    // 广东省
    Map<String, Object> guangdong = new HashMap<>();
    guangdong.put("value", "广东省");
    guangdong.put("label", "广东省");

    List<Map<String, Object>> guangdongCities = new ArrayList<>();

    // 广州市
    Map<String, Object> guangzhou = new HashMap<>();
    guangzhou.put("value", "广州市");
    guangzhou.put("label", "广州市");

    List<Map<String, Object>> guangzhouDistricts = new ArrayList<>();
    String[] gzDistricts = { "越秀区", "海珠区", "荔湾区", "天河区", "白云区", "黄埔区", "番禺区", "花都区", "南沙区", "从化区", "增城区" };
    for (String district : gzDistricts) {
      Map<String, Object> districtMap = new HashMap<>();
      districtMap.put("value", district);
      districtMap.put("label", district);
      guangzhouDistricts.add(districtMap);
    }

    guangzhou.put("children", guangzhouDistricts);
    guangdongCities.add(guangzhou);

    // 深圳市
    Map<String, Object> shenzhen = new HashMap<>();
    shenzhen.put("value", "深圳市");
    shenzhen.put("label", "深圳市");

    List<Map<String, Object>> shenzhenDistricts = new ArrayList<>();
    String[] szDistricts = { "福田区", "罗湖区", "南山区", "盐田区", "宝安区", "龙岗区", "龙华区", "坪山区", "光明区" };
    for (String district : szDistricts) {
      Map<String, Object> districtMap = new HashMap<>();
      districtMap.put("value", district);
      districtMap.put("label", district);
      shenzhenDistricts.add(districtMap);
    }

    shenzhen.put("children", shenzhenDistricts);
    guangdongCities.add(shenzhen);

    guangdong.put("children", guangdongCities);
    provinces.add(guangdong);

    // 添加更多省份
    // 天津市
    provinces.add(createProvince("天津市", new String[][] { {
        "天津市", "和平区", "河东区", "河西区", "南开区", "河北区", "红桥区", "东丽区", "西青区", "津南区", "北辰区", "武清区", "宝坻区", "滨海新区", "宁河区", "静海区",
        "蓟州区"
    } }));

    // 河北省
    provinces.add(createProvince("河北省", new String[][] {
        { "石家庄市", "长安区", "桥西区", "新华区", "井陉矿区", "裕华区", "藁城区", "鹿泉区", "栾城区" },
        { "唐山市", "路南区", "路北区", "古冶区", "开平区", "丰南区", "丰润区", "曹妃甸区" },
        { "秦皇岛市", "海港区", "山海关区", "北戴河区", "抚宁区" },
        { "邯郸市", "邯山区", "丛台区", "复兴区", "峰峰矿区", "肥乡区", "永年区" },
        { "邢台市", "桥东区", "桥西区", "信都区", "临城区", "任泽区", "南和区", "宁晋区" },
        { "保定市", "竞秀区", "莲池区", "满城区", "清苑区", "徐水区" },
        { "张家口市", "桥东区", "桥西区", "宣化区", "下花园区", "万全区", "崇礼区" },
        { "承德市", "双桥区", "双滦区", "鹰手营子矿区", "承德县", "兴隆县", "平泉市" },
        { "沧州市", "新华区", "运河区", "沧县", "青县", "东光县", "海兴县", "盐山县" },
        { "廊坊市", "安次区", "广阳区", "固安县", "永清县", "香河县", "大城县", "文安县", "大厂回族自治县" },
        { "衡水市", "桃城区", "冀州区", "枣强县", "武邑县", "武强县", "饶阳县" }
    }));

    // 山西省
    provinces.add(createProvince("山西省", new String[][] {
        { "太原市", "小店区", "迎泽区", "杏花岭区", "尖草坪区", "万柏林区", "晋源区" },
        { "大同市", "平城区", "云冈区", "新荣区", "云州区" },
        { "阳泉市", "城区", "矿区", "郊区" },
        { "长治市", "潞州区", "上党区", "屯留区", "潞城区", "襄垣县", "平顺县" },
        { "晋城市", "城区", "沁水县", "阳城县", "陵川县", "泽州县", "高平市" },
        { "朔州市", "朔城区", "平鲁区", "山阴县", "应县", "右玉县", "怀仁市" },
        { "晋中市", "榆次区", "太谷区", "榆社县", "左权县", "和顺县", "昔阳县" },
        { "运城市", "盐湖区", "临猗县", "万荣县", "闻喜县", "稷山县", "新绛县" },
        { "忻州市", "忻府区", "定襄县", "五台县", "代县", "繁峙县", "宁武县" },
        { "临汾市", "尧都区", "曲沃县", "翼城县", "襄汾县", "洪洞县", "古县" },
        { "吕梁市", "离石区", "文水县", "交城县", "兴县", "临县", "柳林县" }
    }));

    // 内蒙古自治区
    provinces.add(createProvince("内蒙古自治区", new String[][] {
        { "呼和浩特市", "新城区", "回民区", "玉泉区", "赛罕区", "土默特左旗", "托克托县", "和林格尔县", "清水河县", "武川县" },
        { "包头市", "东河区", "昆都仑区", "青山区", "石拐区", "白云鄂博矿区", "九原区", "土默特右旗", "固阳县", "达尔罕茂明安联合旗" },
        { "乌海市", "海勃湾区", "海南区", "乌达区" },
        { "赤峰市", "红山区", "元宝山区", "松山区", "阿鲁科尔沁旗", "巴林左旗", "巴林右旗", "林西县", "克什克腾旗", "翁牛特旗", "喀喇沁旗", "宁城县", "敖汉旗" },
        { "通辽市", "科尔沁区", "科尔沁左翼中旗", "科尔沁左翼后旗", "开鲁县", "库伦旗", "奈曼旗", "扎鲁特旗", "霍林郭勒市" },
        { "鄂尔多斯市", "东胜区", "康巴什区", "达拉特旗", "准格尔旗", "鄂托克前旗", "鄂托克旗", "杭锦旗", "乌审旗", "伊金霍洛旗" },
        { "呼伦贝尔市", "海拉尔区", "扎赉诺尔区", "阿荣旗", "莫力达瓦达斡尔族自治旗", "鄂伦春自治旗", "鄂温克族自治旗", "陈巴尔虎旗", "新巴尔虎左旗", "新巴尔虎右旗", "满洲里市",
            "牙克石市", "扎兰屯市", "额尔古纳市", "根河市" },
        { "巴彦淖尔市", "临河区", "五原县", "磴口县", "乌拉特前旗", "乌拉特中旗", "乌拉特后旗", "杭锦后旗" },
        { "乌兰察布市", "集宁区", "卓资县", "化德县", "商都县", "兴和县", "凉城县", "察哈尔右翼前旗", "察哈尔右翼中旗", "察哈尔右翼后旗", "四子王旗", "丰镇市" },
        { "兴安盟", "乌兰浩特市", "阿尔山市", "科尔沁右翼前旗", "科尔沁右翼中旗", "扎赉特旗", "突泉县" },
        { "锡林郭勒盟", "二连浩特市", "锡林浩特市", "阿巴嘎旗", "苏尼特左旗", "苏尼特右旗", "东乌珠穆沁旗", "西乌珠穆沁旗", "太仆寺旗", "镶黄旗", "正镶白旗", "正蓝旗",
            "多伦县" },
        { "阿拉善盟", "阿拉善左旗", "阿拉善右旗", "额济纳旗" }
    }));

    // 辽宁省
    provinces.add(createProvince("辽宁省", new String[][] {
        { "沈阳市", "和平区", "沈河区", "大东区", "皇姑区", "铁西区", "苏家屯区", "浑南区", "沈北新区", "于洪区", "辽中区", "康平县", "法库县", "新民市" },
        { "大连市", "中山区", "西岗区", "沙河口区", "甘井子区", "旅顺口区", "金州区", "普兰店区", "瓦房店市", "庄河市" },
        { "鞍山市", "铁东区", "铁西区", "立山区", "千山区", "台安县", "岫岩满族自治县", "海城市" },
        { "抚顺市", "新抚区", "东洲区", "望花区", "顺城区", "抚顺县", "新宾满族自治县", "清原满族自治县" },
        { "本溪市", "平山区", "溪湖区", "明山区", "南芬区", "本溪满族自治县", "桓仁满族自治县" },
        { "丹东市", "元宝区", "振兴区", "振安区", "宽甸满族自治县", "东港市", "凤城市" }
    }));

    return provinces;
  }

  /**
   * 获取所有门店列表
   * 该接口允许未登录用户访问，用于前端展示所有可用的门店
   * 处理地址中的省市区代码，转换为文本形式
   * 
   * @return 所有门店列表
   */
  @GetMapping("/stores")
  public Result<List<Store>> getAllStores() {
    List<Store> stores = storeService.list();

    // 处理地址中的省市区代码，转换为文本形式
    for (Store store : stores) {
      String address = store.getAddress();
      if (address != null && !address.isEmpty()) {
        // 尝试解析地址中的省市区代码
        String[] addressParts = address.split(" ");
        if (addressParts.length >= 3) {
          // 前三部分可能是省市区代码，尝试在区域数据中查找对应的文本
          String province = findRegionNameById(addressParts[0]);
          String city = findRegionNameById(addressParts[1]);
          String district = findRegionNameById(addressParts[2]);

          // 如果成功找到文本表示，则替换地址中的代码
          if (province != null && city != null && district != null) {
            StringBuilder newAddress = new StringBuilder();
            newAddress.append(province).append(" ")
                .append(city).append(" ")
                .append(district);

            // 添加详细地址部分（如果有）
            if (addressParts.length > 3) {
              for (int i = 3; i < addressParts.length; i++) {
                newAddress.append(" ").append(addressParts[i]);
              }
            }

            store.setAddress(newAddress.toString());
          }
        }
      }
    }

    return Result.success(stores);
  }

  /**
   * 根据区域ID查找对应的区域名称
   * 在区域数据中查找匹配的ID，返回对应的名称
   * 
   * @param regionId 区域ID
   * @return 区域名称，如果未找到则返回原ID
   */
  private String findRegionNameById(String regionId) {
    // 遍历所有省份数据
    List<Map<String, Object>> regions = buildRegionData();

    // 尝试查找匹配的省份
    for (Map<String, Object> province : regions) {
      String provinceValue = (String) province.get("value");
      if (provinceValue.equals(regionId)) {
        return (String) province.get("label");
      }

      // 查找城市
      List<Map<String, Object>> cities = (List<Map<String, Object>>) province.get("children");
      if (cities != null) {
        for (Map<String, Object> city : cities) {
          String cityValue = (String) city.get("value");
          if (cityValue.equals(regionId)) {
            return (String) city.get("label");
          }

          // 查找区县
          List<Map<String, Object>> districts = (List<Map<String, Object>>) city.get("children");
          if (districts != null) {
            for (Map<String, Object> district : districts) {
              String districtValue = (String) district.get("value");
              if (districtValue.equals(regionId)) {
                return (String) district.get("label");
              }
            }
          }
        }
      }
    }

    // 如果未找到匹配的区域，则返回原ID
    return regionId;
  }
}