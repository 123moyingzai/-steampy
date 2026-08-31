import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

// 引入Vant组件和样式
import {
  Button,
  Field,
  CellGroup,
  Tabbar,
  TabbarItem,
  NavBar,
  Icon,
  Loading,
  Empty,
  Tag,
  Badge,
  Divider,
  Toast,
  Dialog,
  ActionSheet,
  Popup,
  SwipeCell,
  Checkbox,
  CheckboxGroup,
  Radio,
  RadioGroup,
  Switch,
  Slider,
  Stepper,
  Progress,
  Circle,
  NoticeBar,
  Image as VanImage
} from 'vant'
import 'vant/lib/index.css'

const app = createApp(App)

// 注册Vant组件
app.use(Button)
app.use(Field)
app.use(CellGroup)
app.use(Tabbar)
app.use(TabbarItem)
app.use(NavBar)
app.use(Icon)
app.use(Loading)
app.use(Empty)
app.use(Tag)
app.use(Badge)
app.use(Divider)
app.use(Toast)
app.use(Dialog)
app.use(ActionSheet)
app.use(Popup)
app.use(SwipeCell)
app.use(Checkbox)
app.use(CheckboxGroup)
app.use(Radio)
app.use(RadioGroup)
app.use(Switch)
app.use(Slider)
app.use(Stepper)
app.use(Progress)
app.use(Circle)
app.use(NoticeBar)
app.use(VanImage)

app.use(router)
app.mount('#app')
