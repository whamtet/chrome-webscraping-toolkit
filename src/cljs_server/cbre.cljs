(ns cljs-server.cbre
  (:require
   [crate.core :as crate]
   [cljs-server.core :as core]
   ))

(def PublicationID "4OgtVEy9BKg%3d")

(enable-console-print!)

(js/eval
 "function dl(docId) {

 docId = docId || '4OgtVEy9BKg%3d'
 var resolvedUrl =   '/Layouts/GKCSearch/ValidateDownloadFile.ashx'
 $.ajax({
 url:  resolvedUrl+'?PublicationID=' + docId,
 dataType: 'jsonp',
 crossDomain: true,
 async: false,
 success: function (data) {
 if (data != null)
 {
 var result = '';
 data = $.parseJSON(data);
 $.each(data, function (index, d) {
 result = d.Status;
 });
 console.log('validate ' + result);
 var resolvedDownloadfileUrl = '/Layouts/GKCSearch/DownloadHelper.ashx'
 $.fileDownload(resolvedDownloadfileUrl, {
 failMessageHtml: 'There was a problem generating your report, please try again.'
 });
 }}})
 }")

(def ids (atom nil))

(defn set-ids []
  (let [
        checked (rest (array-seq (js/$ "input:checked")))
        get-edn #(-> % .-parentElement .-parentElement core/dom2edn (get-in [2 2 1 :onclick]))
        get-id #(let [s (re-find #"'.+'" (get-edn %))]
                  (->> s rest butlast (apply str)))
        ]
    (reset! ids (map get-id checked))))

(defn download []
  (if (not-empty @ids)
    (do
      (js/dl (first @ids))
      (swap! ids rest))
    (js/alert "done!!!")))

(println "cbre")
