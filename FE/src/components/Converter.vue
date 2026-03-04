<template>
  <div class="h-full">
    <div class="items-center text-white p-6 bg-pink-900 grid grid-cols-2">
      <div>
        <span class="font-semibold text-xl tracking-tight">CK3 DNA Converter</span>
      </div>
      <div class="text-right text-sm">
        <span> CK3 : v.{{ckVersion}} </span> <br/>
        <span> Updated : {{updatedDate}}</span>
      </div>
    </div>
    <div class="container mx-auto pt-6 h-5/6">
      <div class="grid grid-cols-3 gap-4 h-full">
        <div class="h-full">
          <span class="font-semibold text-xl">DNA</span>
          <textarea class="w-full px-3 py-2 text-gray-700 border rounded-lg focus:outline-none text-sm h-full" v-model="bDna" @input="doit1"/>
        </div>
        <div class="col-span-2">
          <span class="font-semibold text-xl">Ruller Designer</span>
          <textarea class="w-full px-3 py-2 text-gray-700 border rounded-lg focus:outline-none text-xs h-full" v-model="RDna" @input="convertRDna"/>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { indexMap } from '@/files/indexMap.js';
import { childIndexMap } from '@/files/childIndexMap.js';
import { childNameMap } from '@/files/childNameMap.js';
import { info as versions } from '@/files/info.js'

const START_CUSTOM_GENE_INDEX = 3;

export default {
  name: "Converter",
  components: {},
  data() {
    return {
      indexMap : indexMap,
      childIndexMap : childIndexMap,
      childNameMap : childNameMap,

      // versions
      versions: versions,
      updatedDate : "",
      ckVersion: "",

      bDna: "",
      bDnaHexStr: "",

      RDna: "",
      RDnaNameObjArr : [],
      RDnaDigitObjArr : [],
    }
  },
  methods: {

    /**** B-DNA -> R-DNA Converter ***/

    doit1() {
      this.getHexedDna();
      this.hexToRDNADigit();
      this.convertRDnaDigitObjArrToRDnaNameObjArr();
      this.convertArrObjToRDna();
    },

    getHexedDna() {
      this.$data.bDnaHexStr = this.base64ToHex(this.$data.bDna);
    },

    // 숫자 두개씩 끊어서 hex -> num 으로 변경 (05 -> 5)
    // Obj Array 만듦 : [{hair_color : [1,2,3,4]}, {skin_color : [1,2,3,4]}]
    hexToRDNADigit() {
      let str = this.$data.bDnaHexStr;
      let wholeNum = "";
      let tempArr = [];
      let objArr = []
      for (let i = 0; i < str.length; i++) {
        let num = i + 1;
        // 숫자 두개씩 끊어서 hex -> num 으로 변경
        if (i != 0 && num % 2 == 0) {
          let number = this.hexToNumber(str[i-1] + str[i]);
          wholeNum += number;
          tempArr.push(number)
        }

        // 숫자 4개 끊어서 한줄씩 집어넣음
        if (num % 8 == 0) {
          let index = parseInt(i / 8);
          let name = indexMap[index];
          let obj = {};
          obj[name] = tempArr;
          // hair_color = [ 1,2,3,4 ]

          objArr.push(obj);
          tempArr = [];
        }
      }
      this.$data.RDnaDigitObjArr = objArr;
    },

    // R-DNA Digit -> R-DNA 로 변경
    // Before : objArr :  [{gene_chin_forward : [129,2,3,4]}, {skin_color : [1,2,3,4]}]
    // After:  objArr :  [{gene_chin_forward : ["chin_forward_pos",2,"chin_forward_pos",4]}, {skin_color : [1,2,3,4]}]
    // convertRDnaDigitObjArrToRDnaNameObjArr
    convertRDnaDigitObjArrToRDnaNameObjArr() {
      let objArr = this.$data.RDnaDigitObjArr;
      let resultArr = [];
      for (let i = 0; i < objArr.length; i++) {
        let fromObj = objArr[i];
        let key = Object.keys(objArr[i])[0];

        // hair_color ~ 는 고정
        if (i < START_CUSTOM_GENE_INDEX) {
          let obj = {};
          obj[key] = [fromObj[key][0], fromObj[key][1], fromObj[key][2], fromObj[key][3]];
          resultArr.push(obj);
          continue;
        }

        let childMap = childIndexMap[key];            // gene_chin_forward : { "129" : "chin_forward_pos"}
        let obj = {};
        obj[key] = [`"${childMap[fromObj[key][0]]}"`, fromObj[key][1], `"${childMap[fromObj[key][2]]}"`, fromObj[key][3]]
        resultArr.push(obj);
      }
      this.$data.RDnaNameObjArr = resultArr;
    },


    // R-DNA 형식의 String 으로 전환함
    // objArr :  [{hair_color : [1,2,3,4]}, {skin_color : [1,2,3,4]}]
    convertArrObjToRDna() {
      let objArr = this.$data.RDnaNameObjArr;
      let str = "";
      for (let i = 0; i < objArr.length; i++) {
        let obj = objArr[i];
        let key = Object.keys(obj)[0];
        str += `\t\t${key}={ `;
        obj[key].forEach(n => {
          str += `${n} `
        });
        str += "}\n";
      }
      let rDna = this.template_ruller(str);
      this.$data.RDna = rDna;
    },

    // ruller designer 형식으로 변환한다.
    template_ruller(str) {
      return "ruler_designer_234619469={\n"
      + "\tgenes={ \n"
      + str
      + "\t}\n"
      +"}"
    },


    /**** R-DNA -> D-DNA Converter ***/
    convertRDna() {
      this.convertRDnaToArrObj();
      this.convertRDnaNameObjArrToRDnaDigitObjArr();
      this.objArrtoHexString();
      this.setBDna();
    },


    // R-DNA -> ObjArr 형태로 변경
    // ObjArr -> [ {"gene_chin_forward" : ["chin_forward_pos", 129, "chin_forward_pos", 129]} ]
    convertRDnaToArrObj() {
      let geneReg = /([a-zA-Z0-9_]*)\s?=\s?{\s"?([a-zA-Z0-9_]*)"?\s(\d{1,3})\s"?([a-zA-Z0-9_]*)"?\s(\d{1,3})\s}/
      let lines = this.RDna.split('\n');    // 한줄씩 잘라옴
      let objArr = [];

      let geneIndex = 0;

      lines.forEach((line, index) => {
        if (geneReg.test(line)) {
          let objName = RegExp.$1;
          let num1 = RegExp.$2;
          let num2 = RegExp.$3;
          let num3 = RegExp.$4;
          let num4 = RegExp.$5;

          if (indexMap[geneIndex] == objName) {     // indexMap 기준으로 넣는다. indexMap 에 없으면 넣지않음.
            let obj = {};
            obj[objName] = [num1, num2, num3, num4];
            objArr.push(obj)
          }
          geneIndex++;
        }
      })
      this.$data.RDnaNameObjArr = objArr;
    },

    // Before :  ObjArr -> [ {"gene_chin_forward" : ["chin_forward_pos", 129, "chin_forward_pos", 129]} ]
    // After : ObjArr -> [ {"gene_chin_forward" : [0, 129, 0, 129]} ]
    convertRDnaNameObjArrToRDnaDigitObjArr() {
      let arr = this.$data.RDnaNameObjArr;
      let resultArr = []
      for (let i = 0; i < arr.length; i++) {
        let fromObj = arr[i];
        let key = Object.keys(arr[i])[0];

        // 이미 숫자인 경우, 숫자넣음
        if (this.isNumeric(fromObj[key][0])) {
          let obj = {};
          obj[key] = [fromObj[key][0], fromObj[key][1], fromObj[key][2], fromObj[key][3]];
          resultArr.push(obj);
          continue;
        }

        // 문자열인 경우 Map을 활용하여 숫자로 컨버팅한다.
        let childMap = childNameMap[key];  // gene_chin_forward : { "129" : "chin_forward_pos"}
        let obj = {};
        obj[key] = [childMap[fromObj[key][0]], fromObj[key][1], childMap[fromObj[key][2]], fromObj[key][3]];
        resultArr.push(obj);
      }
      this.$data.RDnaDigitObjArr = resultArr;
    },


    // RDnaDigitObjArr 에서 HexString을 생성한다.
    objArrtoHexString() {
      let result = "";
      let arr = this.$data.RDnaDigitObjArr;
      arr.forEach((obj, index) => {
        let key = Object.keys(obj)[0];
        let numArr = obj[key];
        numArr.forEach(n => {
          result += this.numberToHex(n);
        })
      })
      this.$data.bDnaHexStr = result;
    },

    // HexString 을 가지고 와서 Base64 DNA String을 뽑아낸다.
    setBDna() {
      let hexDNA = this.$data.bDnaHexStr;
      let result = this.hexToBase64(hexDNA);
      this.$data.bDna = result;
    },


    /******** CHECKER ********/
    isNumeric(str) {
       if (typeof str != "string") return false
      return !isNaN(str) && !isNaN(parseFloat(str))
    },


    /******** HEX <-> Base64 Converters ********/
    numberToHex(number) {
      let hex = parseInt(number).toString(16);
      return hex.padStart(2, '0');
    },
    hexToNumber(str) {
      return parseInt(str, 16)
    },
    hexToBase64(str) {
      let base64 = "";
        for(let i = 0; i < str.length; i++) {
          base64 += !(i - 1 & 1) ? String.fromCharCode(parseInt(str.substring(i - 1, i + 1), 16)) : ""
        }
      return btoa(base64);
    },
    base64ToHex(str) {
      const raw = atob(str);
      let result = '';
      for (let i = 0; i < raw.length; i++) {
        const hex = raw.charCodeAt(i).toString(16);
        result += (hex.length === 2 ? hex : '0' + hex);
      }
      return result.toUpperCase();
    }
  },
  created() {
    this.ckVersion = versions.rawVersion;

    this.updatedDate = new Date(versions.lastUpdate).toLocaleString()
  }
}
</script>

<style scoped>

</style>