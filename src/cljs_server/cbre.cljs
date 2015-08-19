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
(core/my-get "cbre" #(def downloaded (atom (set %))))

(defn set-ids []
  (let [
        checked (map #(-> % .-parentElement .-parentElement core/dom2edn) (rest (array-seq (js/$ "input:checked"))))
        checked (vec (remove #(@downloaded (last (get-in % [3 2]))) checked))
        ]
    (reset! ids checked)))

(let [
      checked (second (array-seq (js/$ "input:checked")))
      ]
  (-> checked .-parentElement .-parentElement core/dom2edn (get-in [3 2]) last prn))

(defn t []
  (prn @downloaded))

(defn download []
  (if (not-empty @ids)
    (let [id (get-in @ids [0 2 2 1 :onclick])
          download-name (last (get-in @ids [0 3 2]))
          id (re-find #"'.+'" id)
          id (->> id rest butlast (apply str))]
      (js/dl id)
      (swap! ids subvec 1)
      (swap! downloaded conj download-name)
      (core/my-set "cbre" @downloaded)
      )
    (js/alert "done!!!")))

(println "cbre")
