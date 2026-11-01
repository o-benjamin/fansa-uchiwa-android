package com.fansauchiwa.data

/**
 * debug ビルドのホームにだけ出すテンプレートの候補。
 * 渡すのは `app/src/debug` の TemplateCandidateModule だけなので、release と beta には候補が入らない。
 */
class TemplateCandidates(val templates: List<Template>)
