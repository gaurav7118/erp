if(document.addEventListener){document.addEventListener("DOMContentLoaded",function(){document.body.style.backgroundColor="transparent"})}var slicerManifest;function generateSlicerManifest(){var D=document.body.querySelectorAll(".x-slicer-target");var B=[];var E=/^'x-slicer\:(.+)'$/;function A(G){var H=G.getAttribute("data-slicer");if(H){return JSON.parse(H)}return null}function C(J,I){var H=I&&I.content;if(H){var G=E.exec(H);if(G&&G[1]){J.push(G[1])}}}function F(I,H){for(var G=0;G<I.length;++G){H(I[G])}}F(D,function(K){var H=K.ownerDocument.defaultView;var J=H.getComputedStyle(K,null);var I=J["background-image"];var M=K.getBoundingClientRect();var L={box:{x:window.scrollX+M.left,y:window.scrollY+M.top,w:M.right-M.left,h:M.bottom-M.top},radius:{tl:parseInt(J["border-top-left-radius"],10)||0,tr:parseInt(J["border-top-right-radius"],10)||0,br:parseInt(J["border-bottom-right-radius"],10)||0,bl:parseInt(J["border-bottom-left-radius"],10)||0},border:{t:parseInt(J["border-top-width"],10)||0,r:parseInt(J["border-right-width"],10)||0,b:parseInt(J["border-bottom-width"],10)||0,l:parseInt(J["border-left-width"],10)||0}};if(K.id){L.id=K.id}if(I.indexOf("-gradient")!==-1){if(I.indexOf("50% 0")!==-1||I.indexOf("top")!==-1||I.indexOf("bottom")!==-1){L.gradient="top"}else{L.gradient="left"}}var N=[];C(N,H.getComputedStyle(K,":before"));C(N,H.getComputedStyle(K,":after"));if(N.length){L.slices=N.join(", ").split(", ")}var G=A(K);if(G){L.extra=G}B.push(L)});slicerManifest=A(document.body)||{};slicerManifest.widgets=B;if(!slicerManifest.format){slicerManifest.format="2.0"}}