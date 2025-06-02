import { injectGlobalCss } from 'Frontend/generated/jar-resources/theme-util.js';

import { css, unsafeCSS, registerStyles } from '@vaadin/vaadin-themable-mixin';
import $cssFromFile_0 from 'Frontend/generated/jar-resources/styles/toolbar-button.css?inline';
import $cssFromFile_1 from 'print-js/dist/print.css?inline';
import '@vaadin/polymer-legacy-adapter/style-modules.js';
import '@vaadin/vertical-layout/theme/lumo/vaadin-vertical-layout.js';
import 'Frontend/generated/jar-resources/flow-component-renderer.js';
import '@vaadin/popover/theme/lumo/vaadin-popover.js';
import 'Frontend/generated/jar-resources/vaadin-popover/popover.ts';
import '@vaadin/number-field/theme/lumo/vaadin-number-field.js';
import '@vaadin/combo-box/theme/lumo/vaadin-combo-box.js';
import 'Frontend/generated/jar-resources/comboBoxConnector.js';
import '@vaadin/side-nav/theme/lumo/vaadin-side-nav.js';
import 'Frontend/generated/jar-resources/vaadin-grid-flow-selection-column.js';
import '@vaadin/grid/theme/lumo/vaadin-grid-column.js';
import '@vaadin/text-field/theme/lumo/vaadin-text-field.js';
import '@vaadin/integer-field/theme/lumo/vaadin-integer-field.js';
import '@vaadin/icons/vaadin-iconset.js';
import '@vaadin/dialog/theme/lumo/vaadin-dialog.js';
import '@vaadin/accordion/theme/lumo/vaadin-accordion.js';
import '@vaadin/app-layout/theme/lumo/vaadin-app-layout.js';
import '@vaadin/tooltip/theme/lumo/vaadin-tooltip.js';
import '@vaadin/app-layout/theme/lumo/vaadin-drawer-toggle.js';
import '@vaadin/card/theme/lumo/vaadin-card.js';
import '@vaadin/icon/theme/lumo/vaadin-icon.js';
import '@vaadin/upload/theme/lumo/vaadin-upload.js';
import '@vaadin/side-nav/theme/lumo/vaadin-side-nav-item.js';
import '@vaadin/message-input/theme/lumo/vaadin-message-input.js';
import '@vaadin/context-menu/theme/lumo/vaadin-context-menu.js';
import 'Frontend/generated/jar-resources/contextMenuConnector.js';
import 'Frontend/generated/jar-resources/contextMenuTargetConnector.js';
import '@vaadin/horizontal-layout/theme/lumo/vaadin-horizontal-layout.js';
import '@vaadin/multi-select-combo-box/theme/lumo/vaadin-multi-select-combo-box.js';
import '@vaadin/grid/theme/lumo/vaadin-grid.js';
import '@vaadin/grid/theme/lumo/vaadin-grid-sorter.js';
import '@vaadin/checkbox/theme/lumo/vaadin-checkbox.js';
import 'Frontend/generated/jar-resources/gridConnector.ts';
import '@vaadin/button/theme/lumo/vaadin-button.js';
import '@vaadin/details/theme/lumo/vaadin-details.js';
import 'Frontend/generated/jar-resources/disableOnClickFunctions.js';
import 'Frontend/generated/jar-resources/menubarConnector.js';
import '@vaadin/menu-bar/theme/lumo/vaadin-menu-bar.js';
import '@vaadin/grid/theme/lumo/vaadin-grid-column-group.js';
import 'Frontend/generated/jar-resources/messageListConnector.js';
import '@vaadin/message-list/theme/lumo/vaadin-message-list.js';
import 'Frontend/generated/jar-resources/lit-renderer.ts';
import '@vaadin/accordion/theme/lumo/vaadin-accordion-panel.js';
import '@vaadin/notification/theme/lumo/vaadin-notification.js';
import '@vaadin/common-frontend/ConnectionIndicator.js';
import '@vaadin/vaadin-lumo-styles/color-global.js';
import '@vaadin/vaadin-lumo-styles/typography-global.js';
import '@vaadin/vaadin-lumo-styles/sizing.js';
import '@vaadin/vaadin-lumo-styles/spacing.js';
import '@vaadin/vaadin-lumo-styles/style.js';
import '@vaadin/vaadin-lumo-styles/vaadin-iconset.js';
import 'Frontend/generated/jar-resources/ReactRouterOutletElement.tsx';
const $css_0 = typeof $cssFromFile_0  === 'string' ? unsafeCSS($cssFromFile_0) : $cssFromFile_0;
registerStyles('vaadin-button', $css_0, {moduleId: 'flow_css_mod_0'});

injectGlobalCss($cssFromFile_1.toString(), 'CSSImport end', document);

const loadOnDemand = (key) => {
  const pending = [];
  if (key === '3bb125e509fb80e4cef24a95591c81baccdf6ce7b9aa5903ec168330946c12db') {
    pending.push(import('./chunks/chunk-de28a48903eac1008a2de9bbda8c43cf9a17c457cb3e1e079b8e0fa7707af788.js'));
  }
  if (key === '628e5aa9fcb3a08c4bae23001afafa5cb3f3265c773de8b559bc55971696c1f7') {
    pending.push(import('./chunks/chunk-18a963853fce5f7331e6fdd2aef55f0862867fc1a789ee9653e37abde99a16ae.js'));
  }
  if (key === 'b6abd30b3b287759e0d2cb320b6d1f165c62b7b169d9f16381fa2dbb3c6a7048') {
    pending.push(import('./chunks/chunk-1e069e6744f9a7fdcd15042f6893888654fef6378aa8d2f851c4abc7655cf610.js'));
  }
  if (key === 'd23a54d9169597a20f874afe82bf9103eb253950609195ab46968963045cba6a') {
    pending.push(import('./chunks/chunk-4fd173633d55ec275571ff340652fa41b186d7049549847a1e7d8ff502056536.js'));
  }
  if (key === '3328ded69ef47bd1984c6c3b0342a6f09c5fcd78e72756d8caff96a97f91f5ad') {
    pending.push(import('./chunks/chunk-b288a2f0a55bc26dd3e414a4572a53b218611f8efa899450360bee8f5c9d3fd8.js'));
  }
  if (key === '7909deaf75aa089d88b5cc5a7e83243fc1d5e30e33f7f877d6fb464526f2a73b') {
    pending.push(import('./chunks/chunk-896bfbf1a3fd3c6c57ab05bb22acc05b1d556ca29e0f8ba27be3cc8522df5dc8.js'));
  }
  if (key === '61763af01cbf39d122fea6f3876b012dc12200f48991ea83414275f6c8fd9f8a') {
    pending.push(import('./chunks/chunk-b288a2f0a55bc26dd3e414a4572a53b218611f8efa899450360bee8f5c9d3fd8.js'));
  }
  return Promise.all(pending);
}

window.Vaadin = window.Vaadin || {};
window.Vaadin.Flow = window.Vaadin.Flow || {};
window.Vaadin.Flow.loadOnDemand = loadOnDemand;
window.Vaadin.Flow.resetFocus = () => {
 let ae=document.activeElement;
 while(ae&&ae.shadowRoot) ae = ae.shadowRoot.activeElement;
 return !ae || ae.blur() || ae.focus() || true;
}