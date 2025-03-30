/**
 * 省市区三级联动数据
 * 该数据直接在前端使用，无需调用后端API
 */

const regionData = [
  {
    value: "北京市",
    label: "北京市",
    children: [
      {
        value: "北京市",
        label: "北京市",
        children: [
          { value: "东城区", label: "东城区" },
          { value: "西城区", label: "西城区" },
          { value: "朝阳区", label: "朝阳区" },
          { value: "海淀区", label: "海淀区" },
          { value: "丰台区", label: "丰台区" },
          { value: "石景山区", label: "石景山区" },
          { value: "门头沟区", label: "门头沟区" },
          { value: "房山区", label: "房山区" },
          { value: "通州区", label: "通州区" },
          { value: "顺义区", label: "顺义区" },
          { value: "昌平区", label: "昌平区" },
          { value: "大兴区", label: "大兴区" },
          { value: "怀柔区", label: "怀柔区" },
          { value: "平谷区", label: "平谷区" },
          { value: "密云区", label: "密云区" },
          { value: "延庆区", label: "延庆区" }
        ]
      }
    ]
  },
  {
    value: "上海市",
    label: "上海市",
    children: [
      {
        value: "上海市",
        label: "上海市",
        children: [
          { value: "黄浦区", label: "黄浦区" },
          { value: "徐汇区", label: "徐汇区" },
          { value: "长宁区", label: "长宁区" },
          { value: "静安区", label: "静安区" },
          { value: "普陀区", label: "普陀区" },
          { value: "虹口区", label: "虹口区" },
          { value: "杨浦区", label: "杨浦区" },
          { value: "浦东新区", label: "浦东新区" },
          { value: "闵行区", label: "闵行区" },
          { value: "宝山区", label: "宝山区" },
          { value: "嘉定区", label: "嘉定区" },
          { value: "金山区", label: "金山区" },
          { value: "松江区", label: "松江区" },
          { value: "青浦区", label: "青浦区" },
          { value: "奉贤区", label: "奉贤区" },
          { value: "崇明区", label: "崇明区" }
        ]
      }
    ]
  },
  {
    value: "广东省",
    label: "广东省",
    children: [
      {
        value: "广州市",
        label: "广州市",
        children: [
          { value: "越秀区", label: "越秀区" },
          { value: "海珠区", label: "海珠区" },
          { value: "荔湾区", label: "荔湾区" },
          { value: "天河区", label: "天河区" },
          { value: "白云区", label: "白云区" },
          { value: "黄埔区", label: "黄埔区" },
          { value: "番禺区", label: "番禺区" },
          { value: "花都区", label: "花都区" },
          { value: "南沙区", label: "南沙区" },
          { value: "从化区", label: "从化区" },
          { value: "增城区", label: "增城区" }
        ]
      },
      {
        value: "深圳市",
        label: "深圳市",
        children: [
          { value: "福田区", label: "福田区" },
          { value: "罗湖区", label: "罗湖区" },
          { value: "南山区", label: "南山区" },
          { value: "盐田区", label: "盐田区" },
          { value: "宝安区", label: "宝安区" },
          { value: "龙岗区", label: "龙岗区" },
          { value: "龙华区", label: "龙华区" },
          { value: "坪山区", label: "坪山区" },
          { value: "光明区", label: "光明区" }
        ]
      }
    ]
  },
  {
    value: "天津市",
    label: "天津市",
    children: [
      {
        value: "天津市",
        label: "天津市",
        children: [
          { value: "和平区", label: "和平区" },
          { value: "河东区", label: "河东区" },
          { value: "河西区", label: "河西区" },
          { value: "南开区", label: "南开区" },
          { value: "河北区", label: "河北区" },
          { value: "红桥区", label: "红桥区" },
          { value: "东丽区", label: "东丽区" },
          { value: "西青区", label: "西青区" },
          { value: "津南区", label: "津南区" },
          { value: "北辰区", label: "北辰区" },
          { value: "武清区", label: "武清区" },
          { value: "宝坻区", label: "宝坻区" },
          { value: "滨海新区", label: "滨海新区" },
          { value: "宁河区", label: "宁河区" },
          { value: "静海区", label: "静海区" },
          { value: "蓟州区", label: "蓟州区" }
        ]
      }
    ]
  },
  {
    value: "河北省",
    label: "河北省",
    children: [
      {
        value: "石家庄市",
        label: "石家庄市",
        children: [
          { value: "长安区", label: "长安区" },
          { value: "桥西区", label: "桥西区" },
          { value: "新华区", label: "新华区" },
          { value: "井陉矿区", label: "井陉矿区" },
          { value: "裕华区", label: "裕华区" },
          { value: "藁城区", label: "藁城区" },
          { value: "鹿泉区", label: "鹿泉区" },
          { value: "栾城区", label: "栾城区" }
        ]
      },
      {
        value: "唐山市",
        label: "唐山市",
        children: [
          { value: "路南区", label: "路南区" },
          { value: "路北区", label: "路北区" },
          { value: "古冶区", label: "古冶区" },
          { value: "开平区", label: "开平区" },
          { value: "丰南区", label: "丰南区" },
          { value: "丰润区", label: "丰润区" },
          { value: "曹妃甸区", label: "曹妃甸区" }
        ]
      }
    ]
  },
  {
    value: "辽宁省",
    label: "辽宁省",
    children: [
      {
        value: "沈阳市",
        label: "沈阳市",
        children: [
          { value: "和平区", label: "和平区" },
          { value: "沈河区", label: "沈河区" },
          { value: "大东区", label: "大东区" },
          { value: "皇姑区", label: "皇姑区" },
          { value: "铁西区", label: "铁西区" },
          { value: "苏家屯区", label: "苏家屯区" },
          { value: "浑南区", label: "浑南区" },
          { value: "沈北新区", label: "沈北新区" },
          { value: "于洪区", label: "于洪区" },
          { value: "辽中区", label: "辽中区" },
          { value: "康平县", label: "康平县" },
          { value: "法库县", label: "法库县" },
          { value: "新民市", label: "新民市" }
        ]
      },
      {
        value: "抚顺市",
        label: "抚顺市",
        children: [
          { value: "新抚区", label: "新抚区" },
          { value: "东洲区", label: "东洲区" },
          { value: "望花区", label: "望花区" },
          { value: "顺城区", label: "顺城区" },
          { value: "抚顺县", label: "抚顺县" },
          { value: "新宾满族自治县", label: "新宾满族自治县" },
          { value: "清原满族自治县", label: "清原满族自治县" }
        ]
      }
    ]
  }
];

export default regionData;